package designated.director.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import designated.director.repositories.BaseRepository

final case class Draft(id: String, name: String, numOfRounds: Int, numOfTeams: Int)
final case class DraftPost(name: String, numOfRounds: Int, numOfTeams: Int)
final case class Drafts(drafts: Seq[Draft])

object DraftActor {
  final case object GetDrafts
  final case class CreateDraft(draft: DraftPost)
  final case class GetDraft(id: String)
  final case class DeleteDraft(id: String)

  def props: Props = Props[DraftActor]
  def props(repository: BaseRepository[Draft]): Props = Props(new DraftActor(repository))
}

class DraftActor(respository: BaseRepository[Draft]) extends Actor with ActorLogging {
  import DraftActor._

  import context.dispatcher

  override def receive: Receive = {
    case GetDrafts =>
      val o = respository.getAll.map(Drafts)
      o pipeTo sender()
    case CreateDraft(draft) =>
      val d = Draft(UUID.randomUUID().toString, draft.name, draft.numOfRounds, draft.numOfTeams)
      respository.create(d) pipeTo sender()
    case GetDraft(id) =>
      respository get id pipeTo sender()
    case DeleteDraft(id) =>
      respository delete id pipeTo sender()
  }
}
