package designated.director.repositories

import scala.concurrent.{ExecutionContext, Future}
import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import org.neo4j.driver.v1._


case class Connection(uri:String, username: String, password: String)

//TODO: Finish making all methods async
abstract class Neo4jRepository[T](connection: Connection) extends Repository[T] {
  import RepositoryTypes.{AllResults,CreateResult,DeleteResult}
  import org.neo4j.driver.v1.AuthTokens
  import org.neo4j.driver.v1.GraphDatabase

  private[repositories] val driver:Driver =
    GraphDatabase.driver(connection.uri, AuthTokens.basic(connection.username, connection.password))

  val kind:String
  val recordMap:Value => T
  val key: T => String
  val insert: T => String

  def getAll(implicit ex:ExecutionContext): Future[AllResults[T]] = {
    runQueryListAsync(
      new Statement(s"MATCH (n:$kind) RETURN n;"),
      r => {
        recordMap(r.get("n"))
      }
    )
  }

  def get(id: String)(implicit ex:ExecutionContext): Future[Option[T]] = {
    runQueryAsync(
      new Statement(s"MATCH (n:$kind) Where n.id = {id} RETURN n;", Values.parameters("id", id)),
      r => {
        if(r == null) {
          None
        } else {
          Some(recordMap(r.get("n")))
        }
      }
    )
  }

  private[repositories] def tryToCreateResult(record: T, t: Try[String]): CreateResult[T] = t match {
    case Success(v) => Right(record)
    case Failure(e) => Left(e.getLocalizedMessage)
  }

  private[repositories] def tryToDeleteResult(t: Try[String]): DeleteResult = t match {
    case Success(v) => Right(true)
    case Failure(e) => Left(e.getLocalizedMessage)
  }

  def create(record: T)(implicit ex:ExecutionContext): Future[CreateResult[T]] = {
    val statement = s"""CREATE (${key(record)}:$kind ${insert(record)})"""

    runCommandAsync[CreateResult[T]](
      new Statement(statement),
      r => tryToCreateResult(record, r)
    )
  }

  def delete(id: String)(implicit ex:ExecutionContext): Future[DeleteResult] = {
    val statement = new Statement(
      s"""MATCH (k:$kind { id: {id} }) DETACH DELETE k""",
      Values.parameters("id", id)
    )

    runCommandAsync[DeleteResult](
      statement,
      r => tryToDeleteResult(r)
    )
  }

  private[repositories] def runCommandAsync[U](statement:Statement, queryMap:Try[String] => U)(implicit ex:ExecutionContext): Future[U] = {
    val session = driver.session()

    session.runAsync(statement)
      .thenCompose(cursor => {
        cursor.consumeAsync()
      })
      .thenCompose(summary => {
        session.closeAsync()
          .thenApply[U](_ => queryMap(Success(summary.toString)))
      })
      .exceptionally(ex => {
        ex.printStackTrace()
        queryMap(Failure(ex))
      })
      .toScala
  }

  private[repositories] def runQueryAsync[U](statement:Statement, queryMap:Record => U)(implicit ex:ExecutionContext): Future[U] = {
    val session = driver.session()

    session.runAsync(statement)
      .thenCompose(cursor => {
        cursor.singleAsync()
      })
      .thenCompose(record => {
        session.closeAsync()
          .thenApply[U](_ => queryMap(record))
      })
      .exceptionally(ex => {
        ex.printStackTrace()
        queryMap(null)
      })
      .toScala
  }

  private[repositories] def runQueryListAsync[U](statement:Statement, queryMap:Record => U)(implicit ex:ExecutionContext): Future[AllResults[U]] = {
    val session = driver.session()

    session.runAsync(statement)
      .thenCompose(cursor => {
        cursor.listAsync(record => queryMap(record))
      })
      .thenCompose(record => {
        session.closeAsync()
          .thenApply[java.util.List[U]](_ => record)
      })
      .toScala
      .mapTo[java.util.List[U]]
      .map(l => l.asScala)
  }

  private[repositories] def runQuery[U](statement:Statement, queryMap:StatementResult => U)(implicit ex:ExecutionContext): U = {
    val session = driver.session()

    val t = session.readTransaction(new TransactionWork[U]() {
      import org.neo4j.driver.v1.Transaction

      override def execute(tx: Transaction): U = {
        queryMap(tx.run(statement))
      }
    })

    t
  }
}
