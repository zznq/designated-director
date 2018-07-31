package designated.director.repositories

import designated.director.actors.Team
import org.neo4j.driver.v1.{Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}
import java.time.Year

import scala.collection.JavaConverters._
import designated.director.repositories.BaseRepositoryTypes.{AllResults, DeleteResult}

case class TeamRepository(conn: Connection) extends BaseRepository[Team](conn) {
  val kind:String = "Team"

  val recordMap:Value => Team = r => {
    Team(r.get("leagueId").asString(), r.get("id").asString(), r.get("name").asString())
  }

  val key: Team => String = t => t.name.replaceAll(" ", "")
  val insert: Team => String = t => s"""{leagueId: "${t.leagueId}", id:"${t.id}", name:"${t.name}"}"""

  def getAll(leagueId: String)(implicit ex:ExecutionContext): Future[AllResults[Team]] = {
    val params = Values.parameters("lId", leagueId)
    val t = runQuery(
      new Statement(s"MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind) RETURN n;", params),
      rs => {
        rs.list(r => { recordMap(r.get("n")) }).asScala
      }
    )

    Future(t)
  }

  def get(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = {
    val params = Values.parameters("id", id, "lId", leagueId)
    val t = runQuery(
      new Statement(s"MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind { id: {id} }) RETURN n;", params),
      rs => {
        if(rs.hasNext) {
          val r = rs.single()
          Some(recordMap(r.get("n")))
        } else {
          None
        }
      }
    )

    Future(t)
  }

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

  def delete(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[DeleteResult] = {
    val t = runQuery[DeleteResult](
      new Statement(
        s"""MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind { id: {id} }) DETACH DELETE n""",
        Values.parameters("id", id, "lId", leagueId)
      ),
      _ => {
        Right(true)
      }
    )

    Future(t)
  }
}
