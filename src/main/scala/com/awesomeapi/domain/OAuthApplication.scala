package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._

// scalastyle:off public.methods.have.type

case class OAuthApplication(id: Option[Int],
                            name: String,
                            uid: String,
                            secret: String,
                            redirectUri: String,
                            ownerId: Int,
                            ownerType: String,
                            createdAt: Timestamp,
                            updatedAt: Timestamp)

class OAuthApplications(tag: Tag) extends RichTable[OAuthApplication](tag, "OAUTH_APPLICATIONS") {
  def name: Column[String] = column[String]("NAME")
  def uid: Column[String] = column[String]("UID")
  def secret: Column[String]= column[String]("SECRET")
  def redirectUri: Column[String] = column[String]("REDIRECT_URI")
  def ownerId: Column[Int] = column[Int]("OWNER_ID")
  def ownerType: Column[String] = column[String]("OWNER_TYPE")
  def createdAt: Column[Timestamp] = column[Timestamp]("CREATED_AT")
  def updatedAt: Column[Timestamp] = column[Timestamp]("UPDATED_AT")
  def * = (id.?,
           name,
           uid,
           secret,
           redirectUri,
           ownerId,
           ownerType,
           createdAt,
           updatedAt) <> (OAuthApplication.tupled, OAuthApplication.unapply)
}

object OAuthApplications extends CrudTable[OAuthApplications, OAuthApplication]{
  val singleModel = OAuthApplication.getClass
  val tableQuery = TableQuery[OAuthApplications]
}

// scalastyle:on public.methods.have.type
