package designated.director.repositories

import designated.director.actors.Draft
import designated.director.repositories.BaseRepositoryTypes.{AllResults, Result}
import org.neo4j.driver.v1.{Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}

case class DraftRepository(conn: Connection) extends BaseRepository[Draft](conn) with Repository[Draft] {
  val key: Draft => String = d => d.name.replaceAll(" ", "")
  val insert: Draft => String = d =>  s"""{id:"${d.id}", name:"${d.name}", numOfRounds:${d.numOfRounds}, numOfTeams:${d.numOfTeams}}"""

  def getAll(implicit ex:ExecutionContext): Future[AllResults[Draft]] = {
    getList(
      new Statement("MATCH (n:Draft) RETURN n;"),
      r => {
        val v = r.get("n")
        Draft(
          v.get("id").asString(),
          v.get("name").asString(),
          v.get("numOfRounds").asInt(),
          v.get("numOfTeams").asInt()
        )
      }
    )
  }

  def get(id: String)(implicit ex:ExecutionContext): Future[Option[Draft]] = {
    getItem(
      new Statement("MATCH (n:Draft) Where n.id = {id} RETURN n;", Values.parameters("id", id)),
      r => {
        val v = r.get("n")
        Draft(
          v.get("id").asString(),
          v.get("name").asString(),
          v.get("numOfRounds").asInt(),
          v.get("numOfTeams").asInt()
        )
      }
    )
  }

  def create(record: Draft)(implicit ex:ExecutionContext): Future[Result[Draft]] = {
    val statement = new Statement(s"""CREATE (${key(record)}:Draft ${insert(record)})""")

    executeQuery(
      statement,
      record
    )
  }

  def delete(id: String)(implicit ex:ExecutionContext): Future[Result[Boolean]] = {
    val statement = new Statement(
      """MATCH (k:Draft { id: {id} }) DETACH DELETE k""",
      Values.parameters("id", id)
    )

    executeQuery(
      statement,
      true
    )
  }
}
