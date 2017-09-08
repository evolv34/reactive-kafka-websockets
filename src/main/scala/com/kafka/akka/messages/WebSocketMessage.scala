package com.kafka.akka.messages

import spray.json.DefaultJsonProtocol

case class WebSocketMessage(topic: String, content: String)

object WebSocketMessageObject extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(WebSocketMessage)
}