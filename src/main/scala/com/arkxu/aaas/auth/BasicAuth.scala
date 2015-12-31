package com.arkxu.aaas.auth


import akka.http.scaladsl.server.directives.Credentials.{Missing, Provided}
import akka.http.scaladsl.server.directives._
import com.typesafe.config.ConfigFactory

/**
  * Created by arkxu on 12/22/15.
  */
object BasicAuth extends SecurityDirectives {
  val aaasConfig = ConfigFactory.load()
  val username = aaasConfig.getString("aaas.username")
  val password = aaasConfig.getString("aaas.password")

  def authenticator: Authenticator[User] = {
    case p@Provided(username) =>
      if (username == this.username && p.verify(this.password)) Some(User(this.username)) else None
    case Missing => None
  }
}
