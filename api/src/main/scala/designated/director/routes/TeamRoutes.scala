package designated.director.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._

import designated.director.actors.{Team, Teams}
import designated.director.actors.TeamActor.{CreateTeam, DeleteTeam, GetTeam, GetTeams}
import designated.director.api.JsonSupport
import designated.director.repositories.BaseRepositoryTypes._

trait TeamRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val tlog = Logging(system, classOf[Teams])

  def teamActor: ActorRef

  implicit lazy val ttimeout: Timeout = Timeout(5.seconds)

  lazy val teamRoutes: Route =
    pathPrefix("teams") {
      concat(
        pathEnd {
          concat(
            get {
              val ts = (teamActor ? GetTeams).mapTo[Teams]
              val teams = ts.mapTo[Teams]
              complete(teams)
            },
            post {
              entity(as[Team]) { e =>
                val t = (teamActor ? CreateTeam(e)).mapTo[Team]
                onSuccess(t) { performed =>
                  tlog.info("Created Team [{}]", t)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        pathPrefix(Segment) { id =>
          concat(
            get {
              val t = (teamActor ? GetTeam(id)).mapTo[Option[Team]]
              rejectEmptyResponse {
                complete(t)
              }
            },
            delete {
              val t = (teamActor ? DeleteTeam(id)).mapTo[DeleteResult]
              onSuccess(t) { performed =>
                tlog.info("Delete Team [{}]", t)
                complete((StatusCodes.OK, performed.right.get.toString))
              }
            }
          )

        }
      )
    }
}
