package com.awesomeapi.v1.auth

import akka.actor.{Actor, ActorRef, OneForOneStrategy}
import akka.actor.SupervisorStrategy.Escalate
import com.awesomeapi._
import com.awesomeapi.domain.{OAuthAccessToken, ConsumerApplication, User}


class AuthHandlerActor (authServices: Map[Symbol, ActorRef])
  extends Actor with AuthFormats {
  import context._

  var appData  = Option.empty[ConsumerApplication]
  var userData = Option.empty[User]
  var token    = Option.empty[OAuthAccessToken]
  var passwordData = Option.empty[User]
  var isNewUser = false

  private def requestToService[T <: Validable](data: T, appId: Int, actorRef: Symbol) {
    if (!data.isValid) parent ! Error.invalid(data.getErrors)
    else {
      authServices(actorRef) ! data
      authServices('app) ! appId
      become(waitingResponses)
    }
  }

  def receive: Receive = {
    case RegisterRequest(data, appId) => requestToService(data, appId, 'register)
    case LoginRequest(data, appId) => requestToService(data, appId, 'login)
    case FacebookAuthRequest(data, appId) => requestToService(data, appId, 'facebook)
    case UserPasswordRequest(data, appId) => {
      requestToService(data, appId, 'password)
    }
  }

  def waitingResponses: Receive = {
    case (user: User, isNew: Boolean) =>
      userData = Some(user)
      isNewUser = isNew
      replyIfReady()
    case app: ConsumerApplication =>
      appData = Some(app)
      replyIfReady()
    case newToken: OAuthAccessToken =>
      token = Some(newToken)
      replyIfReady()
    case e: Error => parent ! e
  }

  private def replyIfReady(): Unit =
    if (userData.nonEmpty && appData.nonEmpty && token.isEmpty) {
      if (appData.get.isPreauthorized && !appData.get.needsEmailValidation) {
        authServices('token) ! (userData.get.id.get, appData.get)
      } else {
        token = Some(null)
        replyIfReady()
      }
    } else if (userData.nonEmpty && appData.nonEmpty && token.nonEmpty) {
      parent ! (spray.http.StatusCodes.Created, tokenMap(token) ++ userMap(userData.get) ++ appDataMap(appData.get))
    }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case _ => Escalate
    }
}
