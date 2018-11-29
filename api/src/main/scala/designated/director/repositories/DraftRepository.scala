package designated.director.repositories

import designated.director.actors.Draft
import org.neo4j.driver.v1.Value

case class DraftRepository(conn: Connection) extends Neo4jRepository[Draft](conn) with Repository[Draft] {
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
  val insert: Draft => String = d =>  s"""{id:"${d.id}", name:"${d.name}", numOfRounds:${d.numOfRounds}, numOfTeams:${d.numOfTeams}}"""
}
