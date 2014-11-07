package com.awesomeapi.v1.auth

import akka.actor.Props
import spray.httpx.Json4sSupport
import spray.routing._
import org.json4s.JsonAST.JObject
import com.awesomeapi._
import com.awesomeapi.v1.auth.services._
import com.awesomeapi.v1.ApiActor


class AuthActor extends ApiActor with Json4sSupport {

  val authServices = Map(
    'token    -> context.actorOf(Props[TokenServiceActor], "auth_token_service"),
    'register -> context.actorOf(Props[RegisterServiceActor], "auth_register_service"),
    'app      -> context.actorOf(Props[ApplicationDataServiceActor], "auth_app_service"),
    'login    -> context.actorOf(Props[LoginServiceActor], "auth_login_service"),
    'facebook -> context.actorOf(Props[FacebookAuthServiceActor], "auth_facebook_service"),
    'password -> context.actorOf(Props[PasswordServiceActor], "auth_password_service")
  )

  val route = path("token") {
                auth { authToken =>
                  post {
                    entity(as[JObject]) { loginObj =>
                      detach() {
                        authRequest {
                          val loginData = loginObj.extract[UserLoginData]
                          LoginRequest(loginData, authToken.applicationId)
                        }
                      }
                    }
                  }
                }
              } ~
              path("registration") {
                auth { authToken =>
                  post {
                    entity(as[JObject]) { registerObj =>
                      detach() {
                        authRequest {
                          val registrationData = registerObj.extract[UserRegistrationData]
                          RegisterRequest(registrationData, authToken.applicationId)
                        }
                      }
                    }
                  }
                }
              } ~
              path("facebook") {
                auth { authToken =>
                  post {
                    entity(as[JObject]) { facebookObj =>
                      detach() {
                        authRequest {
                          val fbTokenData = facebookObj.extract[FacebookTokenData]
                          FacebookAuthRequest(fbTokenData, authToken.applicationId)
                        }
                      }
                    }
                  }
                }
              } ~
              path("password") {
                auth { authToken =>
                  post {
                    entity(as[JObject]) { passwordObj =>
                      detach() {
                        authRequest {
                          val passData = passwordObj.extract[PasswordData]
                          UserPasswordRequest(passData, authToken.applicationId)
                        }
                      }
                    }
                  }
                }
              }

  def authRequest(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new AuthHandlerActor(authServices)), message)
}
