package com.awesomeapi.v1.auth

import akka.actor.ActorRef
import com.awesomeapi._



//class PasswordActor (passwordService: ActorRef) extends Auth {
//  import context._
//
//  def receive: Receive = {
//    case GetPasswordAuth(user, appId) => {
//      if (EmailValidator.isValid(user.email)){
//        println("D --> paso validacion")
//        passwordService ! user
//        become(waitingResponses)
//      }else{
//        println("D --> no paso validacion")
//        parent ! Error.unprocessableEntity(EmailValidator.message)
//      }
//    }
//  }
//
//  def waitingResponses: Receive = {
//    case user: UserRequestingPassword => {
//      println("D --> encontro usuario")
//      passwordData = Some(user)
//      replyIfReady
//    }
//
//    case None => replyIfReady
//
//    case f: Validation => parent ! f
//    case e: Error      => parent ! e
//  }
//
//  def replyIfReady: Unit =
//    if (passwordData.nonEmpty) {
//      parent ! (spray.http.StatusCodes.Created, "todo ok")
//    } else {
//      parent ! (spray.http.StatusCodes.Created, "kk")
//    }
//}
