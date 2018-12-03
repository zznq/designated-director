package designated.director.repositories

import designated.director.api.JsonSupport
import designated.director.actors.Draft

import org.neo4j.driver.v1.Value
import spray.json._

case class DraftRepository(conn: Connection) extends Neo4jRepository[Draft](conn) with Repository[Draft] with JsonSupport {
  val kind:String = "Draft"

  override val recordMap: Value => Draft = r => {
    Draft(
      r.get("id").asString(),
      r.get("name").asString(),
      r.get("numOfRounds").asInt(),
      r.get("numOfTeams").asInt()
    )
  }

  val key: Draft => String = d => d.name.replaceAll(" ", "")
  val insert: Draft => String = d => d.toJson.compactPrint
}
