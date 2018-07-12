package designated.director.repositories

import designated.director.actors.Team
import org.neo4j.driver.v1.Value

case class TeamRepository() extends BaseRepository[Team] {
  val kind:String = "Team"

  val recordMap:Value => Team = r => {
    Team(r.get("id").asString(), r.get("name").asString())
  }

  val key: Team => String = t => t.name.replaceAll(" ", "")
  val insert: Team => String = t => s"""{id:"${t.id}", name:"${t.name}"}"""
}
