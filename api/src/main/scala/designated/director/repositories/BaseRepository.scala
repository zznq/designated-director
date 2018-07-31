package designated.director.repositories

import org.neo4j.driver.v1._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

case class Connection(uri:String, username: String, password: String)

object BaseRepositoryTypes{
  type DeleteResult = Either[String, Boolean]
  type AllResults[T] = Seq[T]
}

abstract class BaseRepository[T](connection: Connection) {
  import BaseRepositoryTypes._

  import org.neo4j.driver.v1.AuthTokens

  import org.neo4j.driver.v1.GraphDatabase

  private[repositories] val driver:Driver =
    GraphDatabase.driver(connection.uri, AuthTokens.basic(connection.username, connection.password))

  val kind:String
  val recordMap:Value => T
  val key: T => String
  val insert: T => String

  def getAll(implicit ex:ExecutionContext): Future[AllResults[T]] = {
    val t = runQuery(
      new Statement(s"MATCH (n:$kind) RETURN n;"),
      rs => {
        rs.list(r => { recordMap(r.get("n")) }).asScala
      }
    )

    Future(t)
  }

  def get(id: String)(implicit ex:ExecutionContext): Future[Option[T]] = {
    val t = runQuery(
      new Statement(s"MATCH (n:$kind) Where n.id = {id} RETURN n;", Values.parameters("id", id)),
      rs => {
        if(rs.hasNext) {
          val r = rs.single()
          Some(recordMap(r.get("n")))
        } else {
          None
        }
      }
    )

    Future(t)
  }


  def create(record: T)(implicit ex:ExecutionContext): Future[T] = {
    val statement = s"""CREATE (${key(record)}:$kind ${insert(record)})"""

    val t = runQuery(
      new Statement(statement),
      _ => {
        record
      }
    )

    Future(t)
  }

  def delete(id: String)(implicit ex:ExecutionContext): Future[DeleteResult] = {
    val t = runQuery[DeleteResult](
      new Statement(
        s"""MATCH (k:$kind { id: {id} }) DETACH DELETE k""",
        Values.parameters("id", id)
      ),
      _ => {
        Right(true)
      }
    )

    Future(t)
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