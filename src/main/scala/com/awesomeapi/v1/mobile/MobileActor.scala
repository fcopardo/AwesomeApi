package com.awesomeapi.v1.mobile

import akka.actor.Props
import com.awesomeapi._
import com.awesomeapi.routing.XMacValidation
import com.awesomeapi.v1.ApiActor
import com.awesomeapi.v1.mobile.search.{GetSearch, SearchServiceActor, SearchActor}
import spray.routing._


class MobileActor extends ApiActor with XMacValidation {

  val configService = context.actorOf(Props[ConfigServiceActor], "config_service")
  val searchService = context.actorOf(Props[SearchServiceActor], "search_service")


  val route = pass {
    ctx =>
      xTimestamp(ctx) {
        validateXMac(ctx) {
          pathPrefix("auth") {
            ctx => Boot.v1_auth ! ctx
          } ~
          auth {
            authToken =>
              path("config") {
                get {
                  getConfigActor {
                    GetConfig(authToken.applicationId)
                  }
                }
              }
          } ~
          path("search") {
            get{
              getSearchActor {
                GetSearch()
              }
            }
          }
        }
      }(ctx)
  }

  def getConfigActor(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new ConfigActor(configService)), message)

  def getSearchActor(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new SearchActor(searchService)), message)
}
