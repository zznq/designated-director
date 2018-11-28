//package designated.director.actors
//
//import akka.actor.{Actor, OneForOneStrategy}
//import akka.actor.SupervisorStrategy._
//
//import scala.concurrent.duration._
//
//
//class LeagueSupervisor extends Actor {
//  override val supervisorStrategy: OneForOneStrategy = OneForOneStrategy(10, 1 minute) {
//    case _: Exception => Restart
//  }
//}
