package com.awesomeapi.v1

import spray.routing.RequestContext
import com.awesomeapi._
import com.awesomeapi.domain.OAuthAccessToken


package object auth {

  type TokenExtractorType = RequestContext => Option[String]
  type AuthenticatorType = String => Option[OAuthAccessToken]

  case class LoginRequest(data: UserLoginData, app: Int) extends RestMessage
  case class RegisterRequest(data: UserRegistrationData, app: Int) extends RestMessage
  case class FacebookAuthRequest(data: FacebookTokenData, app: Int) extends RestMessage
  case class UserPasswordRequest(data: PasswordData, app: Int) extends RestMessage
}
