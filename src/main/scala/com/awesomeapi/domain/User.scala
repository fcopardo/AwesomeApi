package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.{Date, Timestamp}
import com.awesomeapi.core._
import com.awesomeapi.libs.databaseAccess._
import com.awesomeapi.libs.Environment

// scalastyle:off public.methods.have.type

case class User(
     id: Option[Int],
     email: String,
     password: String,
     gender: Int,
     birthDate: Option[Date],
     createdAt: Timestamp,
     updatedAt: Timestamp
   ) extends BaseModel {
  def birthDateLong: Option[Long] = if (this.birthDate.nonEmpty) Some(this.birthDate.get.getTime / 1000) else None
}

class Users(tag: Tag) extends RichTable[User](tag, "USERS") {
  def email: Column[String]           = column[String]("email")
  def password: Column[String]        = column[String]("encrypted_password")
  def gender: Column[Int]             = column[Int]("gender")
  def birthDate: Column[Option[Date]] = column[Option[Date]]("birth_date")
  def createdAt: Column[Timestamp]    = column[Timestamp]("created_at")
  def updatedAt: Column[Timestamp]    = column[Timestamp]("updated_at")
  def * = (id.?, email, password, gender, birthDate, createdAt, updatedAt) <> (User.tupled, User.unapply)
}

object Users extends CrudTable[Users, User] {
  val singleModel = User.getClass
  val tableQuery = TableQuery[Users]
//  val identities = TableQuery[Identities]

  val seed = com.typesafe.config.ConfigFactory.load("secrets").
                    getString(Environment.env + ".secret_key")
  val tableUserRole = TableQuery[UserRoles]

  private def findByEmailQuery(e: Column[String]) =
    for { u <- tableQuery if u.email === e } yield u
  private val findByEmailQueryCompiled = Compiled(findByEmailQuery _)

  def findByEmail(email: String): Option[User] = db withSession {
    implicit s: Session =>
      findByEmailQueryCompiled(email).firstOption
  }

//  private def findByIdentityQuery(provider: Column[String], id: Column[String]) =
//    for { u <- tableQuery if u.email === e
//    } yield u
//  private val findByIdentityQueryCompiled = Compiled(findByEmailQuery _)
//
//  def findByIdentity(provider: String, id: String): Option[User] = db withSession {
//    implicit s: Session =>
//      findByIdentityQueryCompiled(provider, id).firstOption
//  }

  def getOrCreateUserWithIdentity(user: User, providerType: String, providerUid: String) = ???

  def login(username: String, password: String): Option[User] =
    findByEmail(username) match {
      case Some(u) if u.password == cypherPassword(password) => Some(u)
      case _ => None
    }

  def register(email: String,
               password: String,
               gender: Int,
               birthDate: Option[Long] = None): Option[User] = db withSession {
    implicit s: Session =>
      val ts = now
      val bDay = if (birthDate.nonEmpty) Some(date(birthDate.get)) else None
      val user = User(None, email, password, gender, bDay, ts, ts)
      val userId = (tableQuery returning tableQuery.map(_.id)) insert user
      Some(user.copy(id = Some(userId)))
  }

  def cypherPassword(password: String): String =
    com.awesomeapi.libs.Codecs.sha1(seed + password)
}

// scalastyle:on public.methods.have.type

object Gender extends Enumeration {
  type Gender = Value
  val notKnown      = Value(0, "Not known")
  val male          = Value(1, "Male")
  val female        = Value(2, "Female")
  val notApplicable = Value(9, "Not applicable")
}
