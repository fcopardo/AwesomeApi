package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import com.awesomeapi.domain.{Users, User}
import com.awesomeapi.Error
import com.awesomeapi.v1.auth.UserRegistrationData


class RegisterServiceActor extends Actor {

  def receive: Receive = {
    case reqData: UserRegistrationData =>
      val cypheredPassword = Users.cypherPassword(reqData.password)

      Users.findByEmail(reqData.username) match {
        case Some(u: User) =>
          if (cypheredPassword == u.password) sender ! (u, false)
          else sender ! Error(spray.http.StatusCodes.BadRequest, Some("username already taken"))
        case None =>
          val newUser = Users.register(reqData.username,
                                       cypheredPassword,
                                       reqData.gender,
                                       reqData.birth_date)
          if (newUser.nonEmpty) sender ! (newUser.get, true)
          else sender ! Error.internalServer
      }
  }
}
