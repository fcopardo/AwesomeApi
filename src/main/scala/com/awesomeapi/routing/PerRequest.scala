package com.awesomeapi.routing

import scala.concurrent.duration._
import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.actor.OneForOneStrategy
import spray.http.StatusCode
import spray.http.StatusCodes._
import spray.httpx.Json4sSupport
import spray.routing.RequestContext
import org.json4s.DefaultFormats
import com.awesomeapi._
import com.awesomeapi.routing.PerRequest._


trait PerRequest extends Actor with Json4sSupport {

  import context._

  val json4sFormats = DefaultFormats
  val perRequestTimeOut = 60.seconds

  def r: RequestContext
  def target: ActorRef
  def message: RestMessage

  setReceiveTimeout(perRequestTimeOut)
  target ! message

  def receive: Receive = {
    case (code:StatusCode, obj: AnyRef) => complete(code, obj)
    case res: RestMessage => complete(OK, res)
    case e: Error         => complete(e.code, e.map)
    case ReceiveTimeout   => complete(Error.timedOut.code, Error.timedOut.map)
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T): Unit = {
    if (XMacValidation.enabled) {
      r.withHttpResponseHeadersMapped { headers =>
        XMacValidation.addResponseXMacHeader(headers, obj, XMacValidation.fromHeaders(r.request.headers, XMacValidation.xMacLabel))
      }.complete(status, obj)
    } else r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e: Error => complete(e.code, e.map); Stop
      case e: Throwable => {
        com.awesomeapi.libs.AirbrakeNotifier.notify(e, r, self.path.toString)
        complete(InternalServerError, Error.internalServerMap)
        if (libs.Environment.env != "production") throw e
        Stop
      }
    }
}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: RestMessage): ActorRef =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: RestMessage): ActorRef =
    context.actorOf(Props(new WithProps(r, props, message)))
}
