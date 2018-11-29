package designated.director.api

import java.util.UUID

trait IdGenerator[T] {
  def getNewId: T
}

case class UUIDGenerator() extends IdGenerator[String] {
  override def getNewId: String = UUID.randomUUID().toString
}
