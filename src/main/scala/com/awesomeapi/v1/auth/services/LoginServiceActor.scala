package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import com.awesomeapi.domain.{Users, User}
import com.awesomeapi.Error
import com.awesomeapi.libs.{Codecs, Environment}
import com.awesomeapi.v1.auth.UserLoginData


class LoginServiceActor extends Actor {

  def receive: Receive = {
    case data: UserLoginData => data.grant_type match {
      case "password" =>
        Users.login(data.username, data.password) match {
          case Some(u: User) => sender ! (u, false)
          case None => sender ! Error.unauthorized("invalid username or password")
        }
      case _ => sender ! Error.invalid("invalid grant type")
    }
  }
}
