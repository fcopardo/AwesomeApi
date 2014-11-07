package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import spray.http.StatusCodes._
import com.awesomeapi.domain.{ConsumerApplication, OAuthAccessToken, OAuthAccessTokens}
import com.awesomeapi.Error


class TokenServiceActor extends Actor {

  def receive: Receive = {
    case (userId: Int, app: ConsumerApplication) =>
      val newExpirationTime = app.grantExpiresIn + com.awesomeapi.libs.Time.now

      OAuthAccessTokens.refresh(userId, app.id.get, newExpirationTime, app.scopes) match {
        case Some(token: OAuthAccessToken) => sender ! token
        case None => sender ! Error(InternalServerError.intValue, "NotAvailable", "the token could not be created")
      }
  }
}
