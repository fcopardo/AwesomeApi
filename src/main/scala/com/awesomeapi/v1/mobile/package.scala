package com.awesomeapi.v1

package object mobile {
  import com.awesomeapi.RestMessage

  type LogsConfig = Map[String, Object]
  type VersionsConfig = Map[String, String]

  /* MobileConfig case-classes */
  case class GetConfig(appId: Int) extends RestMessage
  case class MapConfig(config: Config) extends RestMessage
  case class Config(cache_time: Int, versions: VersionsConfig, logs: List[LogsConfig])
}
