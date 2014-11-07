package com.awesomeapi.v1.auth

import com.awesomeapi.domain.{Users, User, ConsumerApplication, OAuthAccessToken}

trait AuthFormats {
  def tokenMap(token: Option[OAuthAccessToken]): Map[String, Any] =
    token match {
      case Some(t) if t != null =>
        Map("token" -> Map( "token_type" -> "bearer",
            "access_token" -> t.token,
            "scope" -> t.scopes,
            "expires_in" -> t.expiresIn))
      case _ => Map("token" -> null)
    }

  def appDataMap(u: ConsumerApplication): Map[String, Any] =
    Map("confirmation_needed" -> u.needsEmailValidation,
        "authorization_needed" -> !u.isPreauthorized
    )

  def userMap(u: User): Map[String, Any] =
    Map("user" -> Map("uuid" -> Users.uuid(u.id.get),
        "email" -> u.email,
        "gender" -> u.gender,
        "birth_date" -> u.birthDateLong))
}
