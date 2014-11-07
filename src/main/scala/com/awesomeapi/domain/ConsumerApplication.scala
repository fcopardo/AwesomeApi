package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._
import com.awesomeapi.libs.databaseAccess._

// scalastyle:off public.methods.have.type

case class ConsumerApplication(id: Option[Int],
                               needsEmailValidation: Boolean,
                               oauthApplicationId: Int,
                               minimalVersion: String,
                               actualVersion: String,
                               cacheTime: Int,
                               grantExpiresIn: Int,
                               scopes: String,
                               isPreauthorized: Boolean,
                               createdAt: Timestamp,
                               updatedAt: Timestamp)

class ConsumerApplications(tag: Tag) extends RichTable[ConsumerApplication](tag, "CONSUMER_APPLICATION_DATA") {
  val grantExpiresInDefault                 = 6570000
  def needsEmailValidation: Column[Boolean] = column[Boolean]("needs_email_validation", O.Default(false))
  def oauthApplicationId: Column[Int]       = column[Int]("oauth_application_id")
  def minimalVersion: Column[String]        = column[String]("minimal_version", O.Default("0.0.0"))
  def actualVersion: Column[String]         = column[String]("actual_version", O.Default("0.0.0"))
  def cacheTime: Column[Int]                = column[Int]("cache_time", O.Default(0))
  def grantExpiresIn: Column[Int]           = column[Int]("grant_expires_in", O.Default(grantExpiresInDefault))
  def scopes: Column[String]                = column[String]("scopes")
  def isPreauthorized: Column[Boolean]      = column[Boolean]("is_preauthorized", O.Default(false))
  def createdAt: Column[Timestamp]          = column[Timestamp]("created_at")
  def updatedAt: Column[Timestamp]          = column[Timestamp]("updated_at")
  def * = (id.?,
    needsEmailValidation,
    oauthApplicationId,
    minimalVersion,
    actualVersion,
    cacheTime,
    grantExpiresIn,
    scopes,
    isPreauthorized,
    createdAt,
    updatedAt) <> (ConsumerApplication.tupled, ConsumerApplication.unapply)
}

object ConsumerApplications extends CrudTable[ConsumerApplications, ConsumerApplication] {
  val singleModel = ConsumerApplication.getClass
  val tableQuery = TableQuery[ConsumerApplications]

  private def findByAppQuery(i: Column[Int]) =
    for (t <- tableQuery if t.oauthApplicationId === i) yield t
  private val findByAppQueryCompiled = Compiled(findByAppQuery _)

  def findByApp(appId: Int): Option[ConsumerApplication] = db withSession {
    implicit s: Session =>
      findByAppQueryCompiled(appId).firstOption
  }
}

// scalastyle:on public.methods.have.type
