package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import com.awesomeapi.core.{CrudTable, RichTable}
import com.awesomeapi.libs.databaseAccess._

// scalastyle:off public.methods.have.type

case class PermissionRole(id: Option[Int],
                             permission_id: Int,
                             role_id: Int)

class PermissionRoles(tag: Tag) extends RichTable[PermissionRole](tag, "permission_roles") {
  def permissionId: Column[Int] = column[Int]("permission_id")
  def roleId: Column[Int]       = column[Int]("role_id")
  def * = (id.?, permissionId, roleId) <> (PermissionRole.tupled, PermissionRole.unapply)
}

object PermissionRoles extends CrudTable[PermissionRoles, PermissionRole]{
  val singleModel = PermissionRole.getClass
  val tableQuery = TableQuery[PermissionRoles]
  val tableRoles = TableQuery[Roles]


  private def findRoleIdsByPermissionQuery(e: Column[Int]) =
    for { pr <- tableQuery if pr.permissionId === e } yield pr.roleId

  private val findRoleIdsByPermissionQueryCompiled = Compiled(findRoleIdsByPermissionQuery _)

  def findRoleIdsByPermission(id: Int): Seq[Int] = db withSession {
    implicit s: Session =>
      findRoleIdsByPermissionQueryCompiled(id).run
  }
}

// scalastyle:on public.methods.have.type
