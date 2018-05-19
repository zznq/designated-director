package designated.director.repositories

import designated.director.actors.Team

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class TeamMemoryRepository() extends BaseRepository[Team] {
  var teams = Set.empty[Team]

  override def getAll(implicit ex:ExecutionContext): Future[Seq[Team]] = Future(teams.toSeq)

  override def get(id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = Future(teams.find(_.id == id))

  override def create(t: Team)(implicit ex:ExecutionContext): Future[Team] = {
    teams += t
    Future(t)
  }

  override def delete(id: String)(implicit ex:ExecutionContext): Future[Either[String, Boolean]] = {
    Try(this.get(id).foreach(team => team.foreach(innerTeam => teams -= innerTeam))) match {
      case Failure(exception) =>
        Future(Left(exception.getMessage))
      case Success(_) =>
        Future(Right(true))
    }
  }
}
