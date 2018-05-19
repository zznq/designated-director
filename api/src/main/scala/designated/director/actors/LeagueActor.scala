package designated.director.actors

import akka.actor.{Actor, ActorLogging, Props}

final case class League(id: String, name: String)
final case class Leagues(leagues: Seq[League])

object LeagueActor {
  final case object GetLeagues
  final case class CreateLeague(league: League)
  final case class GetLeague(id: String)
  final case class DeleteLeauge(id: String)

  def props: Props = Props[LeagueActor]
}

class LeagueActor extends Actor with ActorLogging {
  import LeagueActor._

  var leagues = Set.empty[League]

  override def receive: Receive = {
    case GetLeagues =>
      sender() ! Leagues(leagues.toSeq)
    case CreateLeague(league) =>
      leagues += league
      sender() ! league
    case GetLeague(id) =>
      sender() ! leagues.find(_.id == id)
    case DeleteLeauge(id) =>
      leagues.find(_.id == id) foreach { league => leagues -= league }
      sender() ! "Deleted"
  }
}
