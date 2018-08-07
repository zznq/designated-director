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
import designated.director.repositories.BaseRepositoryTypes.DeleteResult

trait DraftRoutes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Drafts])

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
                val d = (draftActor ? CreateDraft(e)).mapTo[Draft]
                onSuccess(d) { performed =>
                  log.info("Created draft [{}]", d)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
        } ~
        pathPrefix(Segment) { id =>
          pathEnd {
            get {
              val d = (draftActor ? GetDraft(id)).mapTo[Option[Draft]]
              onSuccess(d) { out =>
                val toStatusCode = out.map { _ => StatusCodes.OK }.getOrElse(StatusCodes.NotFound)

                complete((toStatusCode, out))
              }
            } ~
            delete {
              val d = (draftActor ? DeleteDraft(id)).mapTo[DeleteResult]
              onSuccess(d) { performed =>
                log.info("Delete Draft [{}]", d)
                complete((StatusCodes.OK, performed.right.get.toString))
              }
            }
          }
        }
    }
}
