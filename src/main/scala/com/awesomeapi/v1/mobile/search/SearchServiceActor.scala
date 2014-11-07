package com.awesomeapi.v1.mobile.search

import akka.actor.Actor

class SearchServiceActor extends Actor{
  import com.typesafe.config.ConfigFactory

  def receive: Actor.Receive = {
    case ('get, app: Int) => sender() ! ()
    case ('update, _) => ()
  }

}
