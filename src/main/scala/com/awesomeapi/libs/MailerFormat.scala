package com.awesomeapi.libs

import scala.xml._
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Promise, Await, Future}
import com.ning.http.client.Response

object MailerFormat {

  type PropertiesEmail = Array[(String, String)]

  private def getTemplate(template: Symbol): Future[Response] = {
    import dispatch._
    Http(url(Config.url(template)))
  }

  private def formatContentMail(body: String, optionNodes: PropertiesEmail): Future[Elem] = Future{
    <email>
      {optionNodes.flatMap(e => <xml>{e._2}</xml>.copy(label = e._1))}
      <content>{ Unparsed("<![CDATA[%s]]>".format(body)) }</content>
    </email>
  }

  def create(template: Symbol, options: PropertiesEmail): Future[Elem] =  {
    val xmlMail = Promise[Elem]
    val xmlContent = Promise[Response]

    xmlContent completeWith getTemplate(template)
    xmlContent.future onSuccess{
      case r => { xmlMail completeWith formatContentMail(r.getResponseBody, options)}
    }
    xmlMail.future
  }

  def interpolateBody(body: String, variable: Map[String, String]): String = {
    variable.foreach {
      case (key: String, value: String) =>  val key = value
    }
    s""+body
  }


  object Config {
    val conf = com.typesafe.config.ConfigFactory.load("templates")
    val url: Map[Symbol, String] =
      Map('recovery -> conf.getString("mailer.recovery"),
          'confirmation -> conf.getString("mailer.confirmation"))
  }
}
