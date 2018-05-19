package designated.director.actors

import akka.actor.{ Actor, ActorLogging, Props }

final case class Draft(id: String, name: String, numOfRounds: Int, numOfTeams: Int)
final case class Drafts(drafts: Seq[Draft])

object DraftActor {
  final case object GetDrafts
  final case class CreateDraft(draft: Draft)
  final case class GetDraft(id: String)
  final case class DeleteDraft(id: String)

  def props: Props = Props[DraftActor]
}

class DraftActor extends Actor with ActorLogging {
  import DraftActor._

  var drafts = Set.empty[Draft]

  override def receive: Receive = {
    case GetDrafts =>
      sender() ! Drafts(drafts.toSeq)
    case CreateDraft(draft) =>
      drafts += draft
      sender() ! draft
    case GetDraft(id) =>
      sender() ! drafts.find(_.id == id)
    case DeleteDraft(id) =>
      drafts.find(_.id == id) foreach { draft => drafts -= draft }
      sender() ! "Deleted"
  }
}
