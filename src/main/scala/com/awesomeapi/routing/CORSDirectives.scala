package com.awesomeapi.routing

import spray.http._
import spray.routing._
import com.awesomeapi.Error

trait CORSDirectives  { this: HttpService =>

  val OriginCors: List[String] = List("*")
  val CORSError = Error(StatusCodes.Forbidden.intValue, "InvalidOrigin", "The request origin is invalid")

  private def respondWithCORSHeaders(origin: String) =
    respondWithHeaders(
      HttpHeaders.`Access-Control-Allow-Origin`(SomeOrigins(List(origin))),
      HttpHeaders.`Access-Control-Allow-Credentials`(true)
    )
  private def respondWithCORSHeadersAllOrigins =
    respondWithHeaders(
      HttpHeaders.`Access-Control-Allow-Origin`(AllOrigins),
      HttpHeaders.`Access-Control-Allow-Credentials`(true)
    )

  def corsFilter(origins: List[String])(route: Route): Route =
    if (origins.contains("*")){
      respondWithCORSHeadersAllOrigins(route)
    } else {
      optionalHeaderValueByName("Origin") {
        case None =>
          route
        case Some(clientOrigin) => {
          if (origins.contains(clientOrigin)){
            respondWithCORSHeaders(clientOrigin)(route)
          } else {
            throw CORSError
          }
        }
      }
    }
}
