package designated.director.repositories

import designated.director.actors.Team
import org.neo4j.driver.v1.{Statement, Value}

import scala.concurrent.{ExecutionContext, Future}
import java.time.Year

// TODO: Add League constraint to the rest of the methods
case class TeamRepository(conn: Connection) extends BaseRepository[Team](conn) {
  val kind:String = "Team"

  val recordMap:Value => Team = r => {
    Team(r.get("leagueId").asString(), r.get("id").asString(), r.get("name").asString())
  }

  val key: Team => String = t => t.name.replaceAll(" ", "")
  val insert: Team => String = t => s"""{leagueId: "${t.leagueId}", id:"${t.id}", name:"${t.name}"}"""


  override def create(record: Team)(implicit ex:ExecutionContext): Future[Team] = {
    val statement = s"""MATCH (l:League) Where l.id = "${record.leagueId}"
     CREATE (${key(record)}:$kind ${insert(record)})
     CREATE (${key(record)})-[:BELONGS_TO { year: "${Year.now().toString}" }]->(l)"""


    val t = runQuery(
      new Statement(statement),
      _ => {
        record
      }
    )

    Future(t)
  }
}
