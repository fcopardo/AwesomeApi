package com.awesomeapi.domain

abstract class BaseModel {
  def id: Option[Int]

  def uuid: String =
    com.awesomeapi.Uuid.encode(id.get, this.getClass.getSimpleName + "$")
}
