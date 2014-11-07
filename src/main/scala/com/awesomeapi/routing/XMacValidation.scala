package com.awesomeapi.routing

import spray.routing._
import com.awesomeapi.libs.{Time, Codecs, Log}

trait XMacValidation extends HttpService {

  def validateXMac(ctx: RequestContext): Directive0 = {
    if (requestXMacSignature(ctx)) pass
    else complete(XMacRejection.error.code, XMacRejection.errorJson)
  }

  private def requestXMacSignature(ctx: RequestContext): Boolean =
    !XMacValidation.enabled || validateXMacFromContext(ctx)

  private def validateXMacFromContext(ctx: RequestContext): Boolean = {
    val xmacheader = XMacValidation.fromHeaders(ctx.request.headers, XMacValidation.xMacLabel)
    xmacheader.matches("""\d+\-\w+""") && isValidXMac(paramsFromContext(ctx), xmacheader)
  }

  private def paramsFromContext(ctx: RequestContext): List[String] = {
    val query = ctx.request.uri.query.toString
    List(
      extractBodyWithoutImageFromRequest(ctx),
      ctx.request.uri.path.toString + (if (query.length > 0) "?" + query else ""),
      XMacValidation.fromHeaders(ctx.request.headers, "Authorization"),
      XMacValidation.secret
    )
  }

  private def extractBodyWithoutImageFromRequest(ctx: RequestContext): String = {
    if(XMacValidation.fromHeaders(ctx.request.headers, "Content-Type")=="multipart/form-data") {
      import org.json4s._
      import org.json4s.native.JsonMethods._
      import org.json4s.native.Serialization
      import org.json4s.native.Serialization.write

      implicit val formats = Serialization.formats(NoTypeHints)
      val source = parse(ctx.request.entity.asString)
      write(source.replace("image" :: Nil, JNothing))
    } else {
      ctx.request.entity.asString
    }
  }

  private def isValidXMac(params: List[String], xmacheader: String): Boolean = {
    val Array(time, xmac) = xmacheader.split("-")
    xmac == buildXMac(params, time) &&
      (!XMacValidation.time_limited || validTime(time.toLong, Time.now))
  }

  private def buildXMac(params: List[String], time: String): String = {
    val xmac = Codecs.sha1(params.mkString(":") + ":" + time)
    Log.info("XMac: " + xmac)
    xmac
  }

  private def validTime(time_to_eval: Long, actual_time: Long): Boolean =
    (time_to_eval + XMacValidation.time_inf_limit) <= actual_time &&
    (time_to_eval + XMacValidation.time_sup_limit) >= actual_time

}

object XMacValidation {

  import spray.http.{HttpHeader, HttpHeaders}
  import com.typesafe.config.ConfigFactory
  import org.json4s._
  import org.json4s.native.Serialization
  import org.json4s.native.Serialization.write
  import com.awesomeapi.libs.Environment.env

  val xMacLabel = "X-Mac"
  var enabled = true
  var time_limited = false
  var time_inf_limit = 0
  var time_sup_limit = 0
  var secret = ""

  def fromHeaders(headers: List[HttpHeader], headerName: String): String =
    headers.find(_.name == headerName).map(_.value).getOrElse("")

  def addResponseXMacHeader[T <: AnyRef](headers: List[HttpHeader], obj: T, signature: String): List[HttpHeader] =
    buildResponseXMacHeader(obj, signature) :: headers


  def buildResponseXMacHeader[T <: AnyRef](obj: T, signature: String): HttpHeader = {
    implicit val formats = Serialization.formats(NoTypeHints)
    HttpHeaders.RawHeader(xMacLabel, signXMacResponse(write(obj), signature))
  }

  def signXMacResponse(body: String, signature: String): String =
    Codecs.sha1(body + ":" + XMacValidation.secret + ":" + signature)

  def update {
    lazy val conf = ConfigFactory.load("x_mac")
    enabled = conf.getBoolean(env + ".enabled")
    time_limited = conf.getBoolean(env + ".time_limited")
    time_inf_limit = conf.getInt(env + ".time_inf_limit")
    time_sup_limit = conf.getInt(env + ".time_sup_limit")
    secret = conf.getString(env + ".secret")
  }
  update
}

case class XMacRejection(supported: String) extends Rejection
object XMacRejection {
  import com.awesomeapi.Error
  val error = Error(spray.http.StatusCodes.Unauthorized.intValue, "Unauthorized", "Invalid X-Mac Signature")
  val errorJson = error.toJson
}
