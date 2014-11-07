package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import com.awesomeapi.core.{CrudTable, RichTable}
import com.awesomeapi.libs.databaseAccess._

case class UserRole(id: Option[Int],
                   user_id: Int,
                   role_id: Int)

// scalastyle:off public.methods.have.type

class UserRoles(tag: Tag) extends RichTable[UserRole](tag, "user_roles") {
  def userId: Column[Int] = column[Int]("user_id")
  def roleId: Column[Int] = column[Int]("role_id")
  def * = (id.?, userId, roleId) <> (UserRole.tupled, UserRole.unapply)
}

object UserRoles extends CrudTable[UserRoles, UserRole]{
  val singleModel = UserRole.getClass
  val tableQuery = TableQuery[UserRoles]
  val tableRoles = TableQuery[Roles]


  private def findByUserQuery(e: Column[Int]) = for {
    ur <- tableQuery
    r <- tableRoles if ur.userId === e && r.id === ur.roleId
  } yield (r.id, r.name)

  private val findByUserQueryCompiled = Compiled(findByUserQuery _)

  def findByUser(id: Int): Seq[(Int, String)] = db withSession {
    implicit s: Session =>
      findByUserQueryCompiled(id).run
  }
}

// scalastyle:on public.methods.have.type
