package designated.director.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import designated.director.actors.{League, LeagueActor, LeaguePost, Leagues, TeamActor}
import designated.director.api.IdGenerator
import designated.director.repositories.RepositoryTypes.{AllResults, CreateResult, DeleteResult}
import designated.director.repositories.Repository
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

case class MockLeagueRepository() extends Repository[League] {
  private val leagues = Map[String, League](
    "1" -> League("1", "First"),
    "2" -> League("2", "Second"),
    "3" -> League("3", "Third")
  )

  override def getAll(implicit ex: ExecutionContext): Future[AllResults[League]] = {
    Future(leagues.values.toSeq)
  }

  override def get(id: String)(implicit ex: ExecutionContext): Future[Option[League]] = {
    Future(leagues.get(id))
  }

  override def create(record: League)(implicit ex: ExecutionContext): Future[CreateResult[League]] = {
    val out = leagues.get(record.id)

    if(out.isDefined) {
      Future(Left("Record Can't Be Created"))
    } else {
      Future(Right(record))
    }
  }

  override def delete(id: String)(implicit ex: ExecutionContext): Future[DeleteResult] = {
    val out = leagues.get(id)

    if(out.isDefined) {
      Future(Right(true))
    } else {
      Future(Left("Failed to Delete League"))
    }
  }
}

case class MockLeagueIdGenerator() extends IdGenerator[String] {
  override def getNewId: String = "4"
}

class LeagueRoutesTest extends FunSpec with Matchers with ScalatestRouteTest with LeagueRoutes {
  val leagueActor: ActorRef = system.actorOf(LeagueActor.props(MockLeagueRepository(), MockLeagueIdGenerator()), "leagueActor")

  describe("League Routes") {
    describe("/leagues") {
      describe("GET /leagues") {
        it("returns the correct list of leagues") {
          Get("/leagues") ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status should === (StatusCodes.OK)
            responseAs[Leagues] shouldBe Leagues(Seq[League](League("1", "First"), League("2", "Second"), League("3", "Third")))
            responseAs[String] shouldBe """{"leagues":[{"id":"1","name":"First"},{"id":"2","name":"Second"},{"id":"3","name":"Third"}]}"""
          }
        }
      }

      describe("POST /leagues") {
        it("returns created league") {
          val t = LeaguePost("Test League")
          Post("/leagues", t) ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.Created
            responseAs[League] shouldBe League("4", "Test League")
            responseAs[String] shouldBe """{"id":"4","name":"Test League"}"""
          }
        }
//        it("returns 500 when record can't be created") {
//          val t = League("2", "Test League")
//          Post("/leagues", t) ~> leagueRoutes(Seq.empty) ~> check {
//            handled shouldBe true
//            status shouldBe StatusCodes.InternalServerError
//            responseAs[String] shouldBe """Record Can't Be Created"""
//          }
//        }
      }

      describe("GET /leagues/{id}") {
        it("returns the correct league") {
          Get("/leagues/1") ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.OK
            responseAs[League] shouldBe League("1", "First")
            responseAs[String] shouldBe """{"id":"1","name":"First"}"""
          }
        }
        it("returns a 404 response for missing League") {
          Get("/leagues/10000") ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.NotFound
            responseAs[String] shouldBe """No league found with id 10000"""
          }
        }
      }

      describe("DELETE /leagues/{id}") {
        it("returns created league") {
          Delete("/leagues/1") ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.OK
            responseAs[String] shouldBe s"Deleted league with id 1"
          }
        }
        it("returns 500 when record can't be created") {
          val t = League("2", "Test League")
          Delete("/leagues/1000") ~> leagueRoutes(Seq.empty) ~> check {
            handled shouldBe true
            status shouldBe StatusCodes.InternalServerError
            responseAs[String] shouldBe """Failed to Delete League"""
          }
        }
      }
    }
  }
}
