package designated.director.actors


import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import designated.director.api.IdGenerator
import designated.director.repositories.SubRepository

final case class Team(leagueId: String, id: String, name: String)
final case class TeamPost(name: String)
final case class Teams(teams: Seq[Team])

object TeamActor {
  final case class GetTeams(leagueId: String)
  final case class CreateTeam(leagueId: String, team: TeamPost)
  final case class GetTeam(leagueId: String, id: String)
  final case class DeleteTeam(leagueId: String, id: String)

  def props: Props = Props[TeamActor]
  def props(repository: SubRepository[Team], idGenerator: IdGenerator[String]): Props = Props(new TeamActor(repository, idGenerator))
}

class TeamActor(repository:SubRepository[Team], idGenerator: IdGenerator[String]) extends Actor with ActorLogging {
  import TeamActor.{GetTeams,CreateTeam,GetTeam,DeleteTeam}
  import context.dispatcher

  override def receive: Receive = {
    case GetTeams(leagueId) =>
      val o = repository.getAll(leagueId).map(Teams)
      o pipeTo sender()
      
    case CreateTeam(leagueId, team) =>
      val t = Team(leagueId, idGenerator.getNewId, team.name)
      repository.create(t) pipeTo sender()

    case GetTeam(leagueId, id) =>
      repository.get(leagueId, id) pipeTo sender()

    case DeleteTeam(leagueId, id) =>
      val o = repository.delete(leagueId, id)
      o pipeTo sender()
  }
}
