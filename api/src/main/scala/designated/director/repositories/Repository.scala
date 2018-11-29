package designated.director.repositories

import designated.director.repositories.RepositoryTypes.{AllResults, CreateResult, DeleteResult}

import scala.concurrent.{ExecutionContext, Future}

object RepositoryTypes {
  type DeleteResult = Either[String, Boolean]
  type CreateResult[T] = Either[String, T]
  type AllResults[T] = Seq[T]
}

trait Repository[T] {
  def getAll(implicit ex:ExecutionContext): Future[AllResults[T]]
  def get(id: String)(implicit ex:ExecutionContext): Future[Option[T]]
  def create(record: T)(implicit ex:ExecutionContext): Future[CreateResult[T]]
  def delete(id: String)(implicit ex:ExecutionContext): Future[DeleteResult]
}

trait SubRepository[T] {
  def getAll(parentId: String)(implicit ex:ExecutionContext): Future[AllResults[T]]
  def get(parentId: String, id: String)(implicit ex:ExecutionContext): Future[Option[T]]
  def create(record: T)(implicit ex:ExecutionContext): Future[CreateResult[T]]
  def delete(parentId: String, id: String)(implicit ex:ExecutionContext): Future[DeleteResult]
}
