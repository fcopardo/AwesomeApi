package com.awesomeapi.libs

import com.restfb.DefaultFacebookClient
import com.restfb.exception.FacebookOAuthException

object Facebook {

  type FbUser = com.restfb.types.User

  def fbUserFromFbToken(fbToken: String): Option[FbUser] =
    try Some(new DefaultFacebookClient(fbToken).fetchObject("me", classOf[FbUser]))
    catch { case e: FacebookOAuthException => None }
}
