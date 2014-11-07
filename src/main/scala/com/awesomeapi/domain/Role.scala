package com.awesomeapi.domain

import scala.slick.driver.MySQLDriver.simple._
import java.sql.Timestamp
import com.awesomeapi.core._

// scalastyle:off public.methods.have.type

case class Role(id: Option[Int],
                         name: String,
                         createdAt: Timestamp,
                         updatedAt: Timestamp)

class Roles(tag: Tag) extends RichTable[Role](tag, "roles") {
  def name: Column[String]          = column[String]("name")
  def createdAt: Column[Timestamp]  = column[Timestamp]("created_at")
  def updatedAt: Column[Timestamp]  = column[Timestamp]("updated_at")
  def * = (id.?, name, createdAt, updatedAt) <> (Role.tupled, Role.unapply)
}

object Roles extends CrudTable[Roles, Role] {
  val singleModel = Role.getClass
  val tableQuery = TableQuery[Roles]
}

// scalastyle:on public.methods.have.type
