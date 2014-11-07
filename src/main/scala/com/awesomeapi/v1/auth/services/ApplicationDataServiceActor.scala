package com.awesomeapi.v1.auth.services

import akka.actor.Actor
import com.awesomeapi.domain.ConsumerApplications
import com.awesomeapi.Error


class ApplicationDataServiceActor extends Actor {

  def receive: Receive = {
    case appId: Int =>
      ConsumerApplications.findByApp(appId) match {
        case Some(u) => sender ! u
        case None => sender ! Error.unauthorized
      }
  }
}
