package designated.director.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import designated.director.actors._
import spray.json.RootJsonFormat

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  implicit val draftJsonFormat: RootJsonFormat[Draft] = jsonFormat4(Draft)
  implicit val draftPostJsonFormat: RootJsonFormat[DraftPost] = jsonFormat3(DraftPost)
  implicit val draftsJsonFormat: RootJsonFormat[Drafts] = jsonFormat1(Drafts)

  implicit val teamJsonFormat: RootJsonFormat[Team] = jsonFormat2(Team)
  implicit val teamPostJsonFormat: RootJsonFormat[TeamPost] = jsonFormat1(TeamPost)
  implicit val teamsJsonFormat: RootJsonFormat[Teams] = jsonFormat1(Teams)

  implicit val leagueJsonFormat: RootJsonFormat[League] = jsonFormat2(League)
  implicit val leaguePostJsonFormat: RootJsonFormat[LeaguePost] = jsonFormat1(LeaguePost)
  implicit val leaguesJsonFormat: RootJsonFormat[Leagues] = jsonFormat1(Leagues)
}
