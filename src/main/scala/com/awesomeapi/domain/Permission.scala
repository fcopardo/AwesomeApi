package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._

case class Permission(id: Option[Int],
                         action: String,
                         path: String,
                         createdAt: Timestamp,
                         updatedAt: Timestamp)

class Permissions(tag: Tag) extends RichTable[Permission](tag, "permissions") {
  def action: Column[String]        = column[String]("action")
  def path: Column[String]          = column[String]("path")
  def createdAt: Column[Timestamp]  = column[Timestamp]("created_at")
  def updatedAt: Column[Timestamp]  = column[Timestamp]("updated_at")

  // scalastyle:off public.methods.have.type
  def * = (id.?, action, path, createdAt, updatedAt) <> (Permission.tupled, Permission.unapply)
  // scalastyle:on public.methods.have.type
}

object Permissions extends CrudTable[Permissions, Permission] {
  val singleModel = Permission.getClass
  val tableQuery = TableQuery[Permissions]
}
