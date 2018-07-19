package designated.director.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import designated.director.actors.LeagueActor.{CreateLeague, DeleteLeauge, GetLeague, GetLeagues}
import designated.director.actors.{League, LeaguePost, Leagues}
import designated.director.api.JsonSupport
import designated.director.repositories.BaseRepositoryTypes.DeleteResult

import scala.concurrent.duration._

trait LeagueRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val llog = Logging(system, classOf[Leagues])

  def leagueActor: ActorRef

  implicit lazy val ltimeout: Timeout = Timeout(5.seconds)

  lazy val leagueRoutes: Route =
    pathPrefix("leagues") {
      concat(
        pathEnd {
          concat(
            get {
              val leagues = (leagueActor ? GetLeagues).mapTo[Leagues]
              complete(leagues)
            },
            post {
              entity(as[LeaguePost]) { e =>
                val t = (leagueActor ? CreateLeague(e)).mapTo[League]
                onSuccess(t) { performed =>
                  llog.info("Created League [{}]", t)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        pathPrefix(Segment) { id =>
          concat(
            get {
              val l = (leagueActor ? GetLeague(id)).mapTo[Option[League]]
              rejectEmptyResponse {
                complete(l)
              }
            },
            delete {
              val l = (leagueActor ? DeleteLeauge(id)).mapTo[DeleteResult]
              onSuccess(l) { performed =>
                llog.info("Delete League [{}]", l)
                complete((StatusCodes.OK, performed.right.get.toString))
              }
            }
          )

        }
      )
    }
}
