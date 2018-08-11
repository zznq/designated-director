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
import designated.director.actors.{Team, TeamPost, Teams}
import designated.director.actors.TeamActor.{CreateTeam, DeleteTeam, GetTeam, GetTeams}
import designated.director.api.JsonSupport
import designated.director.repositories.BaseRepositoryTypes.Result

trait TeamRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val tlog = Logging(system, classOf[Teams])

  def teamActor: ActorRef

  implicit lazy val ttimeout: Timeout = Timeout(5.seconds)

  def teamRoutes(leagueId: String): Route =
    pathPrefix("teams") {
      pathEnd {
        // GET /teams
        get {
          val ts = (teamActor ? GetTeams(leagueId)).mapTo[Teams]
          val teams = ts.mapTo[Teams]
          complete(teams)
        } ~
        // POST /teams
        post {
          entity(as[TeamPost]) { e =>
            val t = (teamActor ? CreateTeam(leagueId, e)).mapTo[Result[Team]]
            onSuccess(t) { performed =>
              tlog.info("Created Team [{}]", t)
              performed match {
                case Right(l) => complete((StatusCodes.Created, l))
                case Left(message) => complete((StatusCodes.InternalServerError, message))
              }
            }
          }
        }
      } ~
      pathPrefix(Segment) { id =>
        pathEnd {
          // GET /teams/{id}
          get {
            val t = (teamActor ? GetTeam(leagueId, id)).mapTo[Option[Team]]
            rejectEmptyResponse {
              complete(t)
            }
          } ~
          // DELETE /teams/{id}
          delete {
            val t = (teamActor ? DeleteTeam(leagueId, id)).mapTo[Result[Boolean]]
            onSuccess(t) { performed =>
              tlog.info("Delete Team [{}]", t)
              performed match {
                case Right(r) => complete((StatusCodes.OK, r.toString))
                case Left(message) => complete((StatusCodes.InternalServerError, message))
              }
            }
          }
        }
      }
    }
}
