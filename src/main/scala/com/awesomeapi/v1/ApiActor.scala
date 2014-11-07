package com.awesomeapi.v1

import akka.actor.{Actor, ActorContext}
import spray.routing._
import org.json4s.{DefaultFormats, Formats}
import com.awesomeapi.libs._
import com.awesomeapi.routing.{Authorizer, PerRequestCreator, ErrorHandler}

abstract class ApiActor extends Actor with HttpService with PerRequestCreator with ErrorHandler with Authenticator with Authorizer {

  val route: Route

  implicit def json4sFormats: Formats = DefaultFormats
  implicit def actorRefFactory: ActorContext = context

  def receive: Receive = runRoute(
    handleExceptions(exceptionHandler) {
      handleRejections(rejectHandler) {
        route
      }
    }
  )

  def xTimestamp(ctx: RequestContext): Directive0 =
    respondWithHeader(spray.http.HttpHeaders.RawHeader("X-Timestamp", com.awesomeapi.libs.Time.now.toString))
}
