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
import designated.director.actors.{League,Leagues,LeaguePost}
import designated.director.api.JsonSupport
import designated.director.repositories.RepositoryTypes.{CreateResult, DeleteResult}

import scala.concurrent.duration._

trait LeagueRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val llog = Logging(system, classOf[Leagues])

  def leagueActor: ActorRef

  implicit lazy val ltimeout: Timeout = Timeout(5.seconds)

  def leagueRoutes(routes: Seq[String => Route]): Route =
    pathPrefix("leagues") {
      pathEnd {
        // GET /leagues
        get {
          val leagues = (leagueActor ? GetLeagues).mapTo[Leagues]
          complete(leagues)
        } ~
        // POST /leagues
        post {
          entity(as[LeaguePost]) { e =>
            val t = (leagueActor ? CreateLeague(e)).mapTo[CreateResult[League]]
            onSuccess(t) { performed =>
              llog.info("Created League [{}]", t)
              performed match {
                case Right(l) => complete((StatusCodes.Created, l))
                case Left(message) => complete((StatusCodes.InternalServerError, message))
              }
            }
          }
        }
      } ~
      pathPrefix(Segment) { id =>
        // Add routes under /leageus/{id} route
        concat(routes.map(_(id)): _*) ~
        pathEnd {
          // GET /leagues/{id}
          get {
            val l = (leagueActor ? GetLeague(id)).mapTo[Option[League]]
            onSuccess(l) {
              case Some(league) => complete(StatusCodes.OK, league)
              case None => complete(StatusCodes.NotFound, s"No league found with id $id")
            }
          } ~
            // DELETE /leagues/{id}
          delete {
            val l = (leagueActor ? DeleteLeauge(id)).mapTo[DeleteResult]
            onSuccess(l) { performed =>
              llog.info("Delete League [{}]", l)
              performed match {
                case Right(r) => complete((StatusCodes.OK, s"Deleted league with id $id"))
                case Left(message) => complete((StatusCodes.InternalServerError, message))
              }
            }
          }
        }
      }
    }
}
