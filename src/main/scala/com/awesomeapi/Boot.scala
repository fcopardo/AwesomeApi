package com.awesomeapi

import akka.actor.{ActorSystem, Props}
import spray.http.MediaTypes.`application/json`
import spray.routing.{PathMatcher, SimpleRoutingApp}
import com.awesomeapi.routing.{CORSDirectives, ErrorHandler, PermissionAuth}

object Boot extends App with SimpleRoutingApp with ErrorHandler with CORSDirectives {

  implicit val system = ActorSystem("api-system")
  lazy val conf = com.typesafe.config.ConfigFactory.load("application")
  lazy val err_notifier = system.actorOf(Props[com.awesomeapi.libs.AirbrakeNotifier.AirbrakeActor], "airbrake")
  lazy val v1_mobile = system.actorOf(Props[com.awesomeapi.v1.mobile.MobileActor], "mobile")
  lazy val v1_auth = system.actorOf(Props[com.awesomeapi.v1.auth.AuthActor], "auth")

  PermissionAuth.load_permissions

  val Version = PathMatcher( """v([0-9]+)""".r).flatMap {
    case vString => Some(vString)
  }

  startServer(interface = conf.getString("app.server.interface"), port = conf.getInt("app.server.port")) {
    decompressRequest() {
      respondWithMediaType(`application/json`) {
        handleExceptions(exceptionHandler) {
          handleRejections(rejectHandler) {
            corsFilter(OriginCors) {
              pathPrefix(Version) {
                apiVersion => {
                  apiVersion match {
                    case "1" => {
                      pathPrefix("mobile") { ctx => v1_mobile ! ctx} ~
                      pathPrefix("auth") { ctx => v1_auth ! ctx}
                    }
                    case _ => reject
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  system.registerOnTermination {
    system.log.info("API is now down.")
  }
}
