package designated.director.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import designated.director.repositories.Repository

final case class League(id: String, name: String)
final case class LeaguePost(name: String)
final case class Leagues(leagues: Seq[League])

object LeagueActor {
  final case object GetLeagues
  final case class CreateLeague(league: LeaguePost)
  final case class GetLeague(id: String)
  final case class DeleteLeauge(id: String)

  def props: Props = Props[LeagueActor]
  def props(repository: Repository[League]): Props = Props(new LeagueActor(repository))
}

class LeagueActor(repository: Repository[League]) extends Actor with ActorLogging {
  import LeagueActor.{GetLeagues,CreateLeague,GetLeague,DeleteLeauge}
  import context.dispatcher

  override def receive: Receive = {
    case GetLeagues =>
      val o = repository.getAll.map(Leagues)
      o pipeTo sender()
    case CreateLeague(league) =>
      val l = League(UUID.randomUUID().toString, league.name)
      repository.create(l) pipeTo sender()
    case GetLeague(id) =>
      repository get id pipeTo sender()
    case DeleteLeauge(id) =>
      repository delete id pipeTo sender()
  }
}
