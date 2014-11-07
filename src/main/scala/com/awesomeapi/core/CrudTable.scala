package com.awesomeapi.core

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.util._
import com.awesomeapi.libs.databaseAccess._

// scalastyle:off public.methods.have.type

abstract class RichTable[T](tag: Tag, name: String) extends Table[T](tag, name) {
  def id: Column[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
}

trait CrudTable[T <: RichTable[A], A] {

  val tableQuery: TableQuery[T]
  val singleModel: Class[_]

  def insert(model: A) = db withSession {
    implicit s: Session =>
      tableQuery.insert(model)
  }

  def all: CloseableIterator[A] = db withSession {
    implicit s: Session =>
      tableQuery.iterator
  }

  def findById(id: Int): Option[A] = db withSession {
    implicit s: Session =>
      val byId = tableQuery.findBy(_.id)
      byId(id).firstOption
  }

  def delete(id: Int): Boolean = db withSession {
    implicit s: Session =>
      tableQuery.filter(_.id === id).delete > 0
  }

  def update(id: Int, entity: A): Boolean = db withSession {
    implicit s: Session =>
      findById(id) match {
        case Some(e) => {
          tableQuery.filter(_.id === id).update(entity); true
        }
        case None => false
      }
  }

  def uuid(id: Int): String =
    com.awesomeapi.Uuid.encode(id, singleModel.getSimpleName)
}

// scalastyle:on public.methods.have.type
