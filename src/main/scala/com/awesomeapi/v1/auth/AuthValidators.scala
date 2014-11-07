package com.awesomeapi.v1.auth

import com.awesomeapi.Validable

case class UserRegistrationData(
  username: String = "",
  password: String = "",
  password_confirmation: String = "",
  gender: Int = 0,
  birth_date: Option[Long] = None) extends Validable {

  def validate: Unit = {
    if (missing(username)) addError("missing username")
    if (missing(password)) addError("missing password")
    if (missing(password_confirmation)) addError("missing password_confirmation")
    if(password != password_confirmation) addError("password and confirmation doesn't match")
  }

}

case class UserLoginData(
  username: String = "",
  password: String = "",
  grant_type: String = "") extends Validable {

  val grant_types = List("password")
  def validate: Unit = {
    if (!grant_types.contains(grant_type)) addError("invalid grant type")
    if (missing(username)) addError("missing username")
    if (missing(password)) addError("missing password")
  }
}

case class FacebookTokenData(fb_token: String = "") extends Validable {
  def validate: Unit =
    if(missing(fb_token)) addError("missing facebook token")
}

case class PasswordData(email: String = "") extends Validable {
  def validate: Unit =
    if(!validEmail(email)) addError("invalid email")
}
