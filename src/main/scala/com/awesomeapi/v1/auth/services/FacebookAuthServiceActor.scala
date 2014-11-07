package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import com.awesomeapi.domain.{User, Users}
import com.awesomeapi.Error
import com.awesomeapi.libs.Facebook
import com.awesomeapi.libs.Facebook.FbUser
import com.awesomeapi.v1.auth.FacebookTokenData


class FacebookAuthServiceActor extends Actor {

  def receive: Receive = {
    case fbData: FacebookTokenData =>
      val fbUser = Facebook.fbUserFromFbToken(fbData.fb_token)
      if (fbUser.nonEmpty) {
        // TODO: Replace call with following method:
        // Users.getOrCreateUserWithIdentity(user, "facebook", fbUser.getId)
        Users.findById(1) match {
          case Some(u: User) => sender ! (u, false)
          case None => sender ! Error.unauthorized("invalid token")
        }
      } else sender ! Error.unauthorized("invalid token")
  }
}
