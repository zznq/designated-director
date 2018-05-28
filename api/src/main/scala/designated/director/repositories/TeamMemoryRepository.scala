package designated.director.repositories

import java.util.UUID

import designated.director.actors.Team
import org.neo4j.driver.v1.TransactionWork

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class TeamMemoryRepository() extends BaseRepository[Team] {
  import org.neo4j.driver.v1.AuthTokens

  import org.neo4j.driver.v1.GraphDatabase

  val uri = "bolt://localhost:17687"
  val driver = GraphDatabase.driver(uri, AuthTokens.basic("neo4j", "password"))
  var teams = Set.empty[Team]

  override def getAll(implicit ex:ExecutionContext): Future[Seq[Team]] = Future(teams.toSeq)

  override def get(id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = Future(teams.find(_.id == id))

  override def create(t: Team)(implicit ex:ExecutionContext): Future[Team] = {
    val session = driver.session()

    session.writeTransaction(new TransactionWork[String]() {
      import org.neo4j.driver.v1.Transaction

      def execute(tx: Transaction): String = {
        tx.run(s"""CREATE (${t.name.replaceAll(" ", "")}:Team {id:"${t.id}", name:"${t.name}"})""")
        "Created"
      }
    })
    teams += t
    Future(t)
  }

  override def delete(id: String)(implicit ex:ExecutionContext): Future[Either[String, Boolean]] = {
    Try(this.get(id).foreach(team => team.foreach(innerTeam => teams -= innerTeam))) match {
      case Failure(exception) =>
        Future(Left(exception.getMessage))
      case Success(_) =>
        Future(Right(true))
    }
  }
}
