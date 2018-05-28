package designated.director.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import designated.director.repositories.BaseRepository

final case class TeamPost(name:String)
final case class Team(id:String, name: String)
final case class Teams(teams: Seq[Team])

object TeamActor {
  final case object GetTeams
  final case class CreateTeam(team: TeamPost)
  final case class GetTeam(id: String)
  final case class DeleteTeam(id: String)

  def props: Props = Props[TeamActor]
  def props(repository: BaseRepository[Team]): Props = Props(new TeamActor(repository))
}

class TeamActor(repository:BaseRepository[Team]) extends Actor with ActorLogging {
  import TeamActor._

  import context.dispatcher

  override def receive: Receive = {
    case GetTeams =>
      val o = repository.getAll.map(Teams)
      o pipeTo sender()
    case CreateTeam(team) =>
      val t = Team(UUID.randomUUID().toString, team.name)
      repository.create(t) pipeTo sender()
    case GetTeam(id) =>
      repository.get(id) pipeTo sender()
    case DeleteTeam(id) =>
      val o = repository.delete(id)

      o pipeTo sender()
  }

}
