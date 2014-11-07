package com.awesomeapi.libs

object Log {
  import annotation.elidable
  import annotation.elidable._

  // TODO: Disable in production
  def info(str: String): Unit = println(str)
}
