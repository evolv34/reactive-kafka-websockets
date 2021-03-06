package com.kafka.akka.utils

import akka.http.scaladsl.model.HttpRequest
import spray.json.{JsonParser, _}

import scalaj.http.Http

case class AuthResponse(
                         access_token: String,
                         token_type: String,
                         expires_in: String,
                         refresh_token: String)

object AuthResponseObject extends DefaultJsonProtocol {
  implicit val authFormat = jsonFormat4(AuthResponse)
}

object Auth {

  def valid(httpRequest: HttpRequest): Unit = {
    val token = Http("http://localhost:8080/auth").asString.body
    val receivedToken =
      httpRequest
        ._3
        .filter(_.name() == "token")
        .head
        .value

    import AuthResponseObject._
    val authTokenFromServer = JsonParser(token).convertTo[AuthResponse]

    if (!receivedToken.toString.equals(authTokenFromServer.access_token)) {
      throw new Exception("Invalid token")
    }
  }
}
