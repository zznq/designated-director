package designated.director.repositories

import scala.concurrent.{ExecutionContext, Future}

object BaseRepositoryTypes{
  type DeleteResult = Either[String, Boolean]
  type AllResults[T] = Seq[T]
}

trait BaseRepository[T] {
  import BaseRepositoryTypes._

  def getAll(implicit ex:ExecutionContext): Future[AllResults[T]]
  def get(id: String)(implicit ex:ExecutionContext): Future[Option[T]]
  def create(t: T)(implicit ex:ExecutionContext): Future[T]
  def delete(id: String)(implicit ex:ExecutionContext): Future[DeleteResult]
}