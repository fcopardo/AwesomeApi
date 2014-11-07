package com.awesomeapi.v1.mobile.search

import akka.actor.{Actor, ActorRef}
import com.awesomeapi.v1.mobile.search.{MapConfig, GetConfig, Config}


class SearchActor(searchService: ActorRef) extends Actor {
  import context._

  var appConfig = Option.empty[Config]

  def receive : Actor.Receive = {
    case GetConfig(appId) => {
      searchService ! ('get, appId)
      become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case config: Config => {
      appConfig = Some(config)
      replyIfReady
    }
  }

  def replyIfReady: Unit = {
    if(appConfig.nonEmpty) {
      parent ! MapConfig(appConfig.get)
    }
  }
}