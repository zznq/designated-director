package designated.director.repositories

import designated.director.repositories.BaseRepositoryTypes.{AllResults, Result}
import org.neo4j.driver.v1._

import scala.concurrent.{ExecutionContext, Future}
import scala.compat.java8.FutureConverters._
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class Connection(uri:String, username: String, password: String)

object BaseRepositoryTypes{
  type Result[T] = Either[String, T]
  type AllResults[T] = Seq[T]
}

trait Repository[T] {
  def getAll(implicit ex:ExecutionContext): Future[AllResults[T]]
  def get(id: String)(implicit ex:ExecutionContext): Future[Option[T]]
  def create(record: T)(implicit ex:ExecutionContext): Future[Result[T]]
  def delete(id: String)(implicit ex:ExecutionContext): Future[Result[Boolean]]
}

trait ChildRepository[T] {
  def getAll(pid: String)(implicit ex:ExecutionContext): Future[AllResults[T]]
  def get(pid: String, id: String)(implicit ex:ExecutionContext): Future[Option[T]]
  def create(record: T)(implicit ex:ExecutionContext): Future[Result[T]]
  def delete(pid: String, id: String)(implicit ex:ExecutionContext): Future[Result[Boolean]]
}

abstract class BaseRepository[T](connection: Connection) {
  import BaseRepositoryTypes._

  import org.neo4j.driver.v1.AuthTokens

  import org.neo4j.driver.v1.GraphDatabase

  protected val driver:Driver =
    GraphDatabase.driver(connection.uri, AuthTokens.basic(connection.username, connection.password))

  protected def getList(statement: Statement, map:Record => T)(implicit ex:ExecutionContext): Future[AllResults[T]] = {
    runQueryListAsync(
      statement,
      r => map(r)
    )
  }

  protected def getItem(statement: Statement, map:Record => T)(implicit ex:ExecutionContext): Future[Option[T]] = {
    runQueryAsync(
      statement,
      r => {
        if(r == null) {
          None
        } else {
          Some(map(r))
        }
      }
    )
  }

  protected def tryToResult[U](record: U, t: Try[String]): Result[U] = t match {
    case Success(v) => Right(record)
    case Failure(e) => Left(e.getLocalizedMessage)
  }

  protected def executeQuery[U](statement: Statement, out: U)(implicit ex:ExecutionContext): Future[Result[U]] = {
    runCommandAsync(
      statement,
      r => tryToResult[U](out, r)
    )
  }

  protected def runCommandAsync[U](statement:Statement, queryMap:Try[String] => U)(implicit ex:ExecutionContext): Future[U] = {
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

  protected def runQueryAsync[U](statement:Statement, queryMap:Record => U)(implicit ex:ExecutionContext): Future[U] = {
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

  protected def runQueryListAsync[U](statement:Statement, queryMap:Record => U)(implicit ex:ExecutionContext): Future[AllResults[U]] = {
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
}