package designated.director.repositories

import designated.director.actors.Team
import designated.director.api.JsonSupport
import designated.director.repositories.RepositoryTypes.{AllResults, CreateResult, DeleteResult}
import org.neo4j.driver.v1.{Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import java.time.Year


case class TeamRepository(conn: Connection) extends Neo4jRepository[Team](conn) with SubRepository[Team] with JsonSupport {
  val kind: String = "Team"

  val recordMap: Value => Team = r => {
    Team(r.get("leagueId").asString(), r.get("id").asString(), r.get("name").asString())
  }

  val key: Team => String = t => t.name.replaceAll(" ", "")
  val insert: Team => String = _.toJson.compactPrint

  override def getAll(leagueId: String)(implicit ex:ExecutionContext): Future[AllResults[Team]] = {
    val params = Values.parameters("lId", leagueId)
    val statement = new Statement(s"MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind) RETURN n;", params)

    runQueryListAsync(
      statement,
      r => recordMap(r.get("n"))
    )
  }

  override def get(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = {
    val params = Values.parameters("id", id, "lId", leagueId)
    val statement = new Statement(s"MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind { id: {id} }) RETURN n;", params)

    runQueryAsync(
      statement,
      r => Option(r).map(a => recordMap(a.get("n")))
    )
  }

  override def create(record: Team)(implicit ex:ExecutionContext): Future[CreateResult[Team]] = {
    val params = Values.parameters("lId", record.leagueId)
    val statement = new Statement(s"""MATCH (l:League { id: {lId} })
     CREATE (${key(record)}:$kind ${insert(record)})
     CREATE (${key(record)})-[:BELONGS_TO { year: "${Year.now().toString}" }]->(l)""",
      params)

    runCommandAsync(
      statement,
      r => tryToCreateResult(record, r)
    )
  }

  override def delete(leagueId: String, id: String)(implicit ex:ExecutionContext): Future[DeleteResult] = {
    val params = Values.parameters("id", id, "lId", leagueId)
    val statement = new Statement(
      s"""MATCH (l:League { id: {lId} })<-[:BELONGS_TO]-(n:$kind { id: {id} }) DETACH DELETE n""",
      params
    )

    runCommandAsync[DeleteResult](
      statement,
      r => tryToDeleteResult(r)
    )
  }
}
