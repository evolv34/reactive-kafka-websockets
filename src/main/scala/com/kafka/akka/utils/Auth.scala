package com.kafka.akka.utils

import akka.http.scaladsl.model.HttpRequest
import spray.json.JsonParser

import scalaj.http.Http
import spray.json._
import DefaultJsonProtocol._

case class AuthResponse(
                 access_token: String,
                 token_type: String,
                 expires_in: String,
                 refresh_token: String)


object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat4(AuthResponse)
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

    import MyJsonProtocol._
    val authTokenFromServer = JsonParser(token).convertTo[AuthResponse]

    if (!receivedToken.toString.equals(authTokenFromServer.access_token)) {
      throw new Exception("Invalid token")
    }
  }
}
