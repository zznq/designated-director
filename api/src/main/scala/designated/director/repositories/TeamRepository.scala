package designated.director.repositories

import designated.director.actors.Team
import org.neo4j.driver.v1.{Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}
import java.time.Year

import designated.director.repositories.BaseRepositoryTypes.{AllResults, Result}

case class TeamRepository(conn: Connection) extends BaseRepository[Team](conn) with ChildRepository[Team] {
  val key: Team => String = t => t.name.replaceAll(" ", "")
  val insert: Team => String = t => s"""{leagueId: "${t.leagueId}", id:"${t.id}", name:"${t.name}"}"""

  def getAll(leagueId: String)(implicit ex:ExecutionContext): Future[AllResults[Team]] = {
    val params = Values.parameters("lId", leagueId)
    val statement = new Statement("MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:Team) RETURN n;", params)

    getList(
      statement,
      r => {
        val v = r.get("n")
        Team(v.get("leagueId").asString(), v.get("id").asString(), v.get("name").asString())
      }
    )
  }

  def get(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = {
    val params = Values.parameters("id", id, "lId", leagueId)
    val statement = new Statement("MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:Team { id: {id} }) RETURN n;", params)

    getItem(
      statement,
      r => {
        val v = r.get("n")
        Team(v.get("leagueId").asString(), v.get("id").asString(), v.get("name").asString())
      }
    )
  }

  def create(record: Team)(implicit ex:ExecutionContext): Future[Result[Team]] = {
    val params = Values.parameters("lId", record.leagueId)
    val statement = new Statement(s"""MATCH (l:League { id: {lId} })
     CREATE (${key(record)}:Team ${insert(record)})
     CREATE (${key(record)})-[:BELONGS_TO { year: "${Year.now().toString}" }]->(l)""",
    params)

    executeQuery(
      statement,
      record
    )
  }

  def delete(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[Result[Boolean]] = {
    val params = Values.parameters("id", id, "lId", leagueId)
    val statement = new Statement(
      """MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:Team { id: {id} }) DETACH DELETE n""",
      params
    )

    executeQuery(
      statement,
      true
    )
  }
}
