package designated.director.api

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import designated.director.actors.{DraftActor, LeagueActor, TeamActor}
import designated.director.repositories.{Connection, DraftRepository, LeagueRepository, TeamRepository}
import designated.director.routes.{DraftRoutes, LeagueRoutes, TeamRoutes}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class WebServer()

object WebServer extends App with DraftRoutes with LeagueRoutes with TeamRoutes {
  // needed to run the route
  implicit val system: ActorSystem = ActorSystem("DesignatedDirectorWebService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private lazy val log = Logging(system, classOf[WebServer])

  val c = Connection("bolt://localhost:17687", "neo4j", "password")

  val draftActor: ActorRef = system.actorOf(DraftActor.props(DraftRepository(c)), "draftActor")
  val leagueActor: ActorRef = system.actorOf(LeagueActor.props(LeagueRepository(c)), "leagueActor")
  val teamActor: ActorRef = system.actorOf(TeamActor.props(TeamRepository(c)), "teamActor")

  lazy val routes: Route = leagueRoutes(Seq(teamRoutes)) ~ draftRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  log.info(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}

