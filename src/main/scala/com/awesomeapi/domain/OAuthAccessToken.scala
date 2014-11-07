package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._
import com.awesomeapi.libs.Crypt
import com.awesomeapi.libs.databaseAccess._

// scalastyle:off public.methods.have.type

case class OAuthAccessToken(id: Option[Int],
                            resourceOwnerId: Option[Int],
                            applicationId: Int,
                            token: String,
                            scopes: String,
                            expiresIn: Long,
                            createdAt: Timestamp,
                            refreshToken: Option[String] = None,
                            revokedAt: Option[Timestamp] = None
                            )

class OAuthAccessTokens(tag: Tag) extends RichTable[OAuthAccessToken](tag, "OAUTH_ACCESS_TOKENS") {
  def resourceOwnerId: Column[Option[Int]]  = column[Option[Int]]("resource_owner_id", O.Nullable)
  def applicationId: Column[Int]            = column[Int]("application_id")
  def token: Column[String]                 = column[String]("token")
  def scopes: Column[String]                = column[String]("scopes")
  def expiresIn: Column[Long]               = column[Long]("expires_in")
  def createdAt: Column[Timestamp]          = column[Timestamp]("created_at")
  def refreshToken: Column[Option[String]]  = column[Option[String]]("refresh_token", O.Nullable)
  def revokedAt: Column[Option[Timestamp]]  = column[Option[Timestamp]]("revoked_at", O.Nullable)
  def * = (id.?,
           resourceOwnerId,
           applicationId,
           token,
           scopes,
           expiresIn,
           createdAt,
           refreshToken,
           revokedAt) <> (OAuthAccessToken.tupled, OAuthAccessToken.unapply)
}

object OAuthAccessTokens extends CrudTable[OAuthAccessTokens, OAuthAccessToken] {

  val singleModel = OAuthAccessToken.getClass
  val tableQuery = TableQuery[OAuthAccessTokens]

  def refresh(ownerId: Int, appId: Int, expiresIn: Long, scopes: String): Option[OAuthAccessToken] = {
    deleteByUserApp(ownerId, appId)
    create(Some(ownerId), appId, expiresIn, scopes)
  }

  def create(resourceOwnerId: Option[Int],
             applicationId: Int,
             expiresIn: Long,
             scopes: String): Option[OAuthAccessToken] = db withSession {
    implicit s: Session =>

      val token = Crypt.generateToken
      val oAuthToken = OAuthAccessToken(None,
                                        resourceOwnerId,
                                        applicationId,
                                        token,
                                        scopes,
                                        expiresIn,
                                        now)
      val tokenId = (tableQuery returning tableQuery.map(_.id)) insert oAuthToken
      Some(oAuthToken.copy(id = Some(tokenId)))
  }

  def deleteByUserApp(ownerId: Int, appId: Int): Boolean = db withSession {
    implicit s: Session =>
      tableQuery.filter(t => t.resourceOwnerId === ownerId && t.applicationId === appId).delete > 0
  }

  private def findByUserAppQuery(appId: Column[Option[Int]], ownerId: Column[Int]) =
    for { u <- tableQuery if u.applicationId === appId && u.resourceOwnerId === ownerId } yield u
  private val findByUserAppQueryCompiled = Compiled(findByUserAppQuery _)

  def findByUserApp(resourceOwnerId: Option[Int], applicationId: Int) = db withSession {
    implicit s: Session =>
      findByUserAppQueryCompiled(resourceOwnerId, applicationId).firstOption
  }

  private def findByUserQuery(i: Column[Int]) = for {
    u <- tableQuery if u.resourceOwnerId === i
  } yield (u.token, u.expiresIn, u.scopes)

  private val findByUserQueryCompiled = Compiled(findByUserQuery _)
  def findByUser(userId: Int): Option[(String, Long, String)] = db withSession { implicit s: Session =>
    findByUserQueryCompiled(userId).firstOption
  }

  private def findByTokenQuery(k: Column[String]) = for (t <- tableQuery if t.token === k) yield t
  private val findByTokenQueryCompiled = Compiled(findByTokenQuery _)

  def findByToken(token: String): Option[OAuthAccessToken] = db withSession { implicit s: Session =>
    findByTokenQueryCompiled(token).firstOption
  }
}

// scalastyle:on public.methods.have.type
