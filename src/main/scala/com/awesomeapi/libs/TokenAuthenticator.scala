package com.awesomeapi.libs

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing._
import spray.routing.authentication.{Authentication, ContextAuthenticator}
import spray.routing.RequestContext
import com.awesomeapi.domain._
import com.awesomeapi.v1.auth._


object TokenExtraction {
  def fromHeader(headerName: String): TokenExtractorType = { context: RequestContext =>
    context.request.headers.find(_.name == headerName) match {
      case Some(x) if (x.value.matches("""(Bearer)\s[a-zA-Z0-9\.\-]+""")) => Some(x.value.split(" ").last)
      case _  => None
    }
  }

  def fromQueryString(parameterName: String): TokenExtractorType = { context: RequestContext =>
    context.request.uri.query.get(parameterName)
  }
}

object TokenAuthenticator {
  // scalastyle:off public.methods.have.type
  def apply(headerName: String, queryStringParameterName: String)(authenticator: AuthenticatorType)
           (implicit executionContext: ExecutionContext) = {

    def extractor(context: RequestContext): Option[String] =
      TokenExtraction.fromHeader(headerName)(context) orElse
        TokenExtraction.fromQueryString(queryStringParameterName)(context)

    new TokenAuthenticator(extractor, authenticator)
  }
  // scalastyle:on public.methods.have.type
}

class TokenAuthenticator(extractor: TokenExtractorType, authenticator: AuthenticatorType)
                        (implicit executionContext: ExecutionContext) extends ContextAuthenticator[OAuthAccessToken] {

  import AuthenticationFailedRejection._

  def apply(context: RequestContext): Future[Authentication[OAuthAccessToken]] = {
    val reqToken = extractor(context)
    reqToken match {
      case None =>
        Future(
          Left(AuthenticationFailedRejection(CredentialsMissing, List()))
        )
      case Some(token) =>
        val auth = authenticator(token)
        auth match {
          case Some(t) =>
            Future(Right(t))
          case None =>
            Future(Left(AuthenticationFailedRejection(CredentialsRejected, List())))
        }
    }
  }
}

trait Authenticator extends HttpService{
  val authenticator = TokenAuthenticator(
    headerName = "Authorization",
    queryStringParameterName = "access_token"
  ) { token =>
    val date = new java.sql.Date(new java.util.Date().getTime())
    OAuthAccessTokens.findByToken(token)
  }

  def auth: Directive1[OAuthAccessToken] = authenticate(authenticator)

}
