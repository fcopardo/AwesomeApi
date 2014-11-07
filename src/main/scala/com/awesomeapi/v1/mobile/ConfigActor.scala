package com.awesomeapi.v1.mobile

import akka.actor.{ActorRef, Actor}
import com.awesomeapi.domain._

// ConfigActor:
// The Per-request Actor responsible of managing the Config request
class ConfigActor(configService: ActorRef) extends Actor {
  import context._

  var appConfig = Option.empty[Config]

  def receive : Actor.Receive = {
    case GetConfig(appId) => {
      configService ! ('get, appId)
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

// ConfigService Actor:
// Responsible of managing the Config data layer (from DB and config files)
class ConfigServiceActor extends Actor {
  import com.typesafe.config.ConfigFactory

  var cacheTimes: Map[Int, Int] = Map()
  var appVersions: Map[Int, VersionsConfig] = Map()
  var logs: List[LogsConfig] = List()
  var upgradeUrl = ""

  def receive: Actor.Receive = {
    case ('get, app: Int) => sender() ! Config(cacheTimes(app), appVersions(app), logs)
    case ('update, _) => update
  }

  private def update: Unit = {
    import scala.collection.JavaConversions._

    lazy val conf = ConfigFactory.load("logs")
    logs = conf.
      getAnyRefList(com.awesomeapi.libs.Environment.env).toList.
      asInstanceOf[List[java.util.HashMap[String, Object]]].
      map(mapAsScalaMap(_)).filter(_("enabled").asInstanceOf[Boolean]).
      map {
      log =>
        Map(
          "strategy" -> log("strategy"),
          "params" -> mapAsScalaMap(log("params").asInstanceOf[java.util.HashMap[String, String]])
        )
    }

    upgradeUrl = conf.getString("upgrade_url")

    com.awesomeapi.domain.ConsumerApplications.all.foreach {
      app: ConsumerApplication =>
        cacheTimes = cacheTimes ++ Map(
          app.oauthApplicationId -> app.cacheTime
        )
        appVersions = appVersions ++ Map(
          app.oauthApplicationId -> Map(
            "actual" -> app.actualVersion,
            "minimal" -> app.minimalVersion,
            "upgrade_url" -> upgradeUrl
          )
        )
    }
  }
  update
}
