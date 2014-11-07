package com.awesomeapi

import spray.http.StatusCode


// Rest Messages
trait RestMessage

trait Validable {
  type Errors = List[String]
  var errors: Errors = List()
  def addError(s: String): Unit = errors = errors ++ List(s)
  def missing(s: String): Boolean = s.length == 0
  def validEmail(s: String): Boolean =
    """\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*\b""".r.unapplySeq(s).isDefined
  def getErrors: String = errors.mkString(", ")
  def validate(): Unit
  def isValid: Boolean = {
    errors = List()
    validate()
    errors.isEmpty
  }
}

// UUID
case class Uuid(id: Int, signature: String) {
  val uuid = Uuid.encode(id, signature)
  override def toString: String = uuid
}

object Uuid {
  import com.awesomeapi.libs.Crypt

  val exception = new IllegalArgumentException("Invalid UUID")

  def encode(id: Int, signature: String): String =
    Crypt.encrypt(signature + ":" + f"$id%024d")

  def decode(uuid: String): (Int, String) = {
    val Array(signature, preId) = Crypt.decrypt(uuid).split(":")
    (preId.toInt, signature)
  }

  def extract(uuid: String, signature: String): Int = {
      val (id, sign) = decode(uuid)
      if (sign != signature) throw exception
      else id
  }
}

// Json
abstract class JSON
case class JSeq(elems: List[JSON])            extends JSON
case class JObj(bindings: Map[String, JSON])  extends JSON
case class JLong(num: Double)                 extends JSON
case class JNum(num: Int)                     extends JSON
case class JStr(str: String)                  extends JSON
case class JBool(b: Boolean)                  extends JSON
case object JNull                             extends JSON

// Errors

case class Error(code: Int, `type`: String, message: String, errors: Option[Seq[String]] = None) extends Throwable {
  import org.json4s.native.Serialization._
  import org.json4s.{DefaultFormats, Formats}

  implicit def json4sFormats: Formats = DefaultFormats
  def map: Map[String, Error] = Map("error" -> this)
  def toJson: String = write(this.map)
}

object Error {
  import spray.http.StatusCodes._

  def apply[T <: StatusCode](err: T, message: Option[String] = None): Error =
    Error(err.intValue, err.reason, message.getOrElse(err.defaultMessage))

  def unauthorized(): Error = Error(Unauthorized, None)
  def unauthorized(message: String): Error = Error(Unauthorized, Some(message))
  def unprocessableEntity(): Error = Error(UnprocessableEntity, None)
  def unprocessableEntity(message: String): Error = Error(UnprocessableEntity, Some(message))

  def invalid(message: String): Error = Error(BadRequest, Some(message))

  val internalServer     = Error(InternalServerError)
  val internalServerMap  = internalServer.map
  val internalServerJson = internalServer.toJson

  val invalidPathJson    = Error(NotFound.intValue, "NotFound", "Invalid path", None).toJson
  val timedOut           = Error(InternalServerError.intValue, "ServerTimeOut", "The request processing was taking too long")
  val forbidden          = Error(Forbidden.intValue, "Forbidden", "You don't have permissions to access")
}
