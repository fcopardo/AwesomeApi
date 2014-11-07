package com.awesomeapi.libs

import scala.xml._
import akka.actor.Actor
import spray.routing._


object AirbrakeNotifier {

  def formatStacktrace(traceElements: Array[StackTraceElement]) : Array[Node] = {
    traceElements.flatMap(e => {
        <line method={e.getMethodName} file={e.getFileName} number={e.getLineNumber.toString}/>
    })
  }

  def formatParams(params: Map[String,List[String]]) : NodeSeq = {
    <params>{params.flatMap(e => {
      <var key={e._1}>{e._2.mkString(" ")}</var>
    })}</params>
  }

  def notify(e: Throwable, ctx: RequestContext, path: String): Unit = if (Config.enabled) notifyEnabled(e, ctx, path)

  def notifyEnabled(e: Throwable, ctx: RequestContext, path: String) {
    // More information about API definiton @https://help.airbrake.io/kb/api-2/notifier-api-version-23
    val request = <notice version="2.3">
      <api-key>{Config.apiKey}</api-key>
      <notifier>
        <name>{Config.notifierName}</name>
        <version>0.0.1</version>
        <url>{Config.notifierUrl}</url>
      </notifier>
      <error>
        <class>{e.getClass.getName}</class>
        <message>{e.getMessage}</message>
        <backtrace>
          {formatStacktrace(e.getStackTrace)}
        </backtrace>
      </error>
      <request>
        <url>{ctx.request.uri.path.toString}</url>
        {formatParams(Map("entity" -> List(ctx.request.entity.asString)) ++ ctx.request.uri.query.toMultiMap)}
        <component>{path}</component>
        <action/>
      </request>
      <server-environment>
        <environment-name>{Environment.env}</environment-name>
      </server-environment>
    </notice>

    com.awesomeapi.Boot.err_notifier ! AirbrakeNotice(request)
  }

  def sendtoAirbrake(xml: NodeSeq) {
    import dispatch._
    import concurrent.ExecutionContext.Implicits.global
    Http(url(Config.url) << xml.toString <:< Config.headers)
  }

  case class AirbrakeNotice(xml: NodeSeq)

  class AirbrakeActor extends Actor {
    def receive: Receive = {
      case AirbrakeNotice(xml) => sendtoAirbrake(xml)
      case _ => ()
    }
  }

  object Config {
    val conf = com.typesafe.config.ConfigFactory.load("airbrake")
    val enabled = conf.getBoolean(Environment.env + ".enabled")
    val apiKey = conf.getString("airbrake.api_key")
    val url = conf.getString("airbrake.endpoint") + ":" +
      conf.getInt("airbrake.port").toString + "/notifier_api/v2/notices"
    val notifierName = conf.getString("airbrake.notifier_name")
    val notifierUrl = conf.getString("airbrake.notifier_url")
    val headers = Map(
      "Accept" -> "text/xml, application/xml",
      "Content-type" -> "text/xml"
    )
  }
}
