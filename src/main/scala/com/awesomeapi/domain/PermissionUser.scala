package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import com.awesomeapi.core.{CrudTable, RichTable}
import com.awesomeapi.libs.databaseAccess._

// scalastyle:off public.methods.have.type

case class PermissionUser(id: Option[Int],
                         permission_id: Int,
                         user_id: Int)

class PermissionUsers(tag: Tag) extends RichTable[PermissionUser](tag, "permission_users") {
  def permissionId: Column[Int] = column[Int]("permission_id")
  def userId: Column[Int]       = column[Int]("user_id")
  def * = (id.?, permissionId, userId) <> (PermissionUser.tupled, PermissionUser.unapply)
}

object PermissionUsers extends CrudTable[PermissionUsers, PermissionUser]{
  val singleModel = PermissionUser.getClass
  val tableQuery = TableQuery[PermissionUsers]
  val tableUser = TableQuery[Users]

  private def findUserIdsByPermissionQuery(pId: Column[Int]) =
    for { pu <- tableQuery if pu.permissionId === pId } yield pu.userId

  private val findUserIdsByPermissionQueryCompiled = Compiled(findUserIdsByPermissionQuery _)

  def findUserIdsByPermission(permissionId: Int): Seq[Int] = db withSession {
    implicit s: Session =>
      findUserIdsByPermissionQueryCompiled(permissionId).run
  }
}

// scalastyle:on public.methods.have.type
