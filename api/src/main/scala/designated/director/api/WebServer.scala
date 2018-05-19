package designated.director.api

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import designated.director.actors.{DraftActor, LeagueActor, TeamActor}
import designated.director.repositories.TeamMemoryRepository
import designated.director.routes.{DraftRoutes, LeagueRoutes, TeamRoutes}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object WebServer extends App with DraftRoutes with LeagueRoutes with TeamRoutes {

  // needed to run the route
  implicit val system: ActorSystem = ActorSystem("DesignatedDirectorWebService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val draftActor: ActorRef = system.actorOf(DraftActor.props, "draftActor")
  val leagueActor: ActorRef = system.actorOf(LeagueActor.props, "leagueActor")
  val teamActor: ActorRef = system.actorOf(TeamActor.props(TeamMemoryRepository()), "teamActor")

  lazy val routes: Route = draftRoutes ~ leagueRoutes ~ teamRoutes

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}

