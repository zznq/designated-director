package designated.director.repositories

import designated.director.actors.League
import org.neo4j.driver.v1.{Record, Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}

case class LeagueRepository(conn: Connection) extends BaseRepository[League](conn) {
  val kind:String = "League"

  val recordMap:Value => League = r => {
    League(r.get("id").asString(), r.get("name").asString())
  }

  val key: League => String = t => t.name.replaceAll(" ", "")
  val insert: League => String = t => s"""{id:"${t.id}", name:"${t.name}"}"""
}
