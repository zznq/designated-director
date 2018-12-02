package designated.director.repositories

import designated.director.actors.League
import designated.director.api.JsonSupport

import org.neo4j.driver.v1.Value
import spray.json._

case class LeagueRepository(conn: Connection) extends Neo4jRepository[League](conn) with Repository[League] with JsonSupport {
  val kind:String = "League"

  val recordMap:Value => League = r => {
    League(r.get("id").asString(), r.get("name").asString())
  }

  val key: League => String = t => t.name.replaceAll(" ", "")
  val insert: League => String = _.toJson.compactPrint
}
