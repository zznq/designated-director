package designated.director.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{get, post}
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import designated.director.actors.DraftActor.{CreateDraft, DeleteDraft, GetDraft, GetDrafts}
import designated.director.actors.{Draft, DraftPost, Drafts}

import scala.concurrent.duration._
import akka.pattern.ask
import designated.director.api.JsonSupport

import designated.director.repositories.RepositoryTypes.{CreateResult, DeleteResult}


trait DraftRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val dlog = Logging(system, classOf[Drafts])

  // other dependencies that UserRoutes use
  def draftActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout: Timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val draftRoutes: Route =
    pathPrefix("drafts") {
        //# Draft Status
        pathEnd {
          get {
            val drafts = (draftActor ? GetDrafts).mapTo[Drafts]
            complete(drafts)
          } ~
            post {
              entity(as[DraftPost]) { e =>
                val d = (draftActor ? CreateDraft(e)).mapTo[CreateResult[Draft]]
                onSuccess(d) { performed =>
                  dlog.info("Created draft [{}]", d)
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
            get {
              val d = (draftActor ? GetDraft(id)).mapTo[Option[Draft]]
              rejectEmptyResponse {
                complete(d)
              }
            } ~
            delete {
              val d = (draftActor ? DeleteDraft(id)).mapTo[DeleteResult]
              onSuccess(d) { performed =>
                dlog.info("Delete Draft [{}]", d)
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
