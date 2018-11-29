package designated.director.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import designated.director.actors.{Team, TeamActor, Teams}
import designated.director.repositories.RepositoryTypes.{AllResults, CreateResult, DeleteResult}
import designated.director.repositories.SubRepository
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

case class MockTeamRepository() extends SubRepository[Team] {
  private val teams = Map[String, Map[String, Team]](
                "1" -> Map("1" -> Team("1", "1", "First")),
                "2" -> Map("1" -> Team("2", "1", "Second"), "2" -> Team("2", "2", "Third"))
  )

  override def getAll(leagueId: String)(implicit ex: ExecutionContext): Future[AllResults[Team]] = {
    val out = teams.get(leagueId)
        .map(_.values.toSeq)
        .getOrElse(Seq.empty)

    Future(out)
  }

  override def get(leagueId: String, id: String)(implicit ex: ExecutionContext): Future[Option[Team]] = {
    val out = teams.get(leagueId)
      .flatMap(_.get(id))

    Future(out)
  }

  override def create(record: Team)(implicit ex: ExecutionContext): Future[CreateResult[Team]] = {
    val out = teams.get(record.leagueId)
      .flatMap(_.get(record.id))

    if(out.isDefined) {
      Future(Left("Record Can't Be Created"))
    } else {
      Future(Right(record))
    }
  }

  override def delete(leagueId: String, id: String)(implicit ex: ExecutionContext): Future[DeleteResult] = {
    val out = teams.get(leagueId)
      .flatMap(_.get(id))

    if(out.isDefined) {
      Future(Right(true))
    } else {
      Future(Left("Record Doesn't Exist"))
    }


  }
}

class TeamRoutesTest extends FunSpec with Matchers with ScalatestRouteTest with TeamRoutes {

  val teamActor: ActorRef = system.actorOf(TeamActor.props(MockTeamRepository()), "teamActor")

  describe("Team Routes") {
    describe("/teams") {
      describe("GET /teams") {
        it("returns the correct list of teams") {
          Get("/teams") ~> teamRoutes("1") ~> check {
            handled shouldBe true
            status should === (StatusCodes.OK)
            responseAs[Teams] shouldBe Teams(Seq[Team](Team("1", "1", "First")))
            responseAs[String] shouldBe """{"teams":[{"leagueId":"1","id":"1","name":"First"}]}"""
          }
        }
      }

      describe("GET /teams/{id}") {
        it("returns the correct team") {
          Get("/teams/1") ~> teamRoutes("1") ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.OK
            responseAs[Team] shouldBe Team("1", "1", "First")
            responseAs[String] shouldBe """{"leagueId":"1","id":"1","name":"First"}"""
          }
        }
        it("returns a 404 response") {
          Get("/teams/10000") ~> teamRoutes("1") ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.NotFound
            responseAs[String] shouldBe """No team found in league 1 with id 10000"""
          }
        }
        it("returns a 404 response for missing league") {
          Get("/teams/1") ~> teamRoutes("1000") ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.NotFound
            responseAs[String] shouldBe """No team found in league 1000 with id 1"""
          }
        }
      }
    }
  }
}
