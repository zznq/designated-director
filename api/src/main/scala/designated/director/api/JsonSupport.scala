package designated.director.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import designated.director.actors._

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._

  implicit val draftJsonFormat = jsonFormat4(Draft)
  implicit val draftsJsonFormat = jsonFormat1(Drafts)

  implicit val teamJsonFormat = jsonFormat2(Team)
  implicit val teamsJsonFormat = jsonFormat1(Teams)

  implicit val leagueJsonFormat = jsonFormat2(League)
  implicit val leaguesJsonFormat = jsonFormat1(Leagues)
}
