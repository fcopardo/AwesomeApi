package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._

// scalastyle:off public.methods.have.type

case class Identity (
  id: Option[Int],
  userId: Int,
  providerType: Int,
  uid: String,
  username: Option[String],
  token: Option[String],
  secret: Option[String],
  providerUpdatedAt: Option[Timestamp],
  createdAt: Timestamp,
  updatedAt: Timestamp)

class Identities(tag: Tag) extends RichTable[Identity](tag, "identities") {
  def userId: Column[Int]                           = column[Int]("user_id")
  def providerType: Column[Int]                     = column[Int]("provider_type")
  def uid: Column[String]                           = column[String]("uid")
  def username: Column[Option[String]]              = column[Option[String]]("username")
  def token: Column[Option[String]]                 = column[Option[String]]("token")
  def secret: Column[Option[String]]                = column[Option[String]]("secret")
  def providerUpdatedAt: Column[Option[Timestamp]]  = column[Option[Timestamp]]("provider_updated_at")
  def createdAt: Column[Timestamp]                  = column[Timestamp]("created_at")
  def updatedAt: Column[Timestamp]                  = column[Timestamp]("updated_at")
  def * = ( id.?,
            userId,
            providerType,
            uid,
            username,
            token,
            secret,
            providerUpdatedAt,
            createdAt,
            updatedAt) <> (Identity.tupled, Identity.unapply)

}

object Identities extends CrudTable[Identities, Identity] {
  val singleModel = Identity.getClass
  val tableQuery = TableQuery[Identities]
}

// scalastyle:on public.methods.have.type
