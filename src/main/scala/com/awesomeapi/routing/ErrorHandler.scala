package com.awesomeapi.routing

import spray.http.StatusCodes._
import spray.routing.{ExceptionHandler, HttpService, RejectionHandler}
import spray.util.LoggingContext
import com.awesomeapi._


trait ErrorHandler extends HttpService {

  val rejectHandler = RejectionHandler {
    case Nil => complete(NotFound, Error.invalidPathJson)
  }

  implicit def exceptionHandler(implicit log: LoggingContext): ExceptionHandler =
    ExceptionHandler.apply {
      case e: Error => complete(e.code, e.toJson)
      case e: Throwable => ctx => {
        com.awesomeapi.libs.AirbrakeNotifier.notify(e, ctx, this.getClass.getName)
        ctx.complete(InternalServerError, Error.internalServerJson)
        if (libs.Environment.env != "production") throw e
      }
    }
}
