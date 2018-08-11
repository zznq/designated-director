package designated.director.repositories

import designated.director.actors.{Draft, League}
import designated.director.repositories.BaseRepositoryTypes.{AllResults, Result}
import org.neo4j.driver.v1.{Record, Statement, Value, Values}

import scala.concurrent.{ExecutionContext, Future}

case class LeagueRepository(conn: Connection) extends BaseRepository[League](conn) with Repository[League] {
  val key: League => String = t => t.name.replaceAll(" ", "")
  val insert: League => String = t => s"""{id:"${t.id}", name:"${t.name}"}"""

  def getAll(implicit ex:ExecutionContext): Future[AllResults[League]] = {
    getList(
      new Statement("MATCH (n:League) RETURN n;"),
      r => {
        val v = r.get("n")
        League(v.get("id").asString(), v.get("name").asString())
      }
    )
  }

  def get(id: String)(implicit ex:ExecutionContext): Future[Option[League]] = {
    getItem(
      new Statement(s"MATCH (n:League) Where n.id = {id} RETURN n;", Values.parameters("id", id)),
      r => {
        val v = r.get("n")
        League(v.get("id").asString(), v.get("name").asString())
      }
    )
  }

  def create(record: League)(implicit ex:ExecutionContext): Future[Result[League]] = {
    val statement = new Statement(s"""CREATE (${key(record)}:League ${insert(record)})""")

    executeQuery(
      statement,
      record
    )
  }

  def delete(id: String)(implicit ex:ExecutionContext): Future[Result[Boolean]] = {
    val statement = new Statement(
      s"""MATCH (k:League { id: {id} }) DETACH DELETE k""",
      Values.parameters("id", id)
    )

    executeQuery(
      statement,
      true
    )
  }
}
