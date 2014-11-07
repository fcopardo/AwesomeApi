package com.awesomeapi.libs

object Environment {
  val env = sys.env.getOrElse("ENV","development")
}
