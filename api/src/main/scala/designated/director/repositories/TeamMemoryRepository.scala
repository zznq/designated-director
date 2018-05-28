package designated.director.repositories

import designated.director.actors.Team

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import org.neo4j.driver.v1.{Statement, TransactionWork, Values}

case class TeamMemoryRepository() extends BaseRepository[Team] {
  import org.neo4j.driver.v1.AuthTokens

  import org.neo4j.driver.v1.GraphDatabase

  private val uri = "bolt://localhost:17687"
  private val driver = GraphDatabase.driver(uri, AuthTokens.basic("neo4j", "password"))
  private var teams = Set.empty[Team]

  override def getAll(implicit ex:ExecutionContext): Future[Seq[Team]] = {
    val session = driver.session()

    val t = session.readTransaction(new TransactionWork[Seq[Team]]() {
      import org.neo4j.driver.v1.Transaction

      def execute(tx: Transaction): Seq[Team] = {
        val s = new Statement("MATCH (n:Team) RETURN n;")
        val rs = tx.run(s)

        rs
          .list(r => {
            Team(r.get("n").get("id").asString(), r.get("n").get("name").asString())
          })
          .asScala
      }
    })

    Future(t)
  }

  override def get(id: String)(implicit ex:ExecutionContext): Future[Option[Team]] = {
    val session = driver.session()

    val t = session.readTransaction(new TransactionWork[Option[Team]]() {
      import org.neo4j.driver.v1.Transaction

      def execute(tx: Transaction): Option[Team] = {
        val s = new Statement("MATCH (t:Team) WHERE t.id = $id RETURN t;")
        val r = tx.run(s.withParameters(Values.parameters("id", id)))

        if(r.hasNext) {
          val record = r.single()
          Some(Team(record.get("t").get("id").asString(), record.get("t").get("name").asString()))
        } else {
          None
        }
      }
    })

    Future(t)
  }

  override def create(t: Team)(implicit ex:ExecutionContext): Future[Team] = {
    val session = driver.session()
    session.writeTransaction(new TransactionWork[String]() {
      import org.neo4j.driver.v1.Transaction

      def execute(tx: Transaction): String = {
        tx.run(s"""CREATE (${t.name.replaceAll(" ", "")}:Team {id:"${t.id}", name:"${t.name}"})""")
        "Created"
      }
    })

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
