package com.kafka.akka.routes

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import com.kafka.akka.KafkaProducerService
import com.kafka.akka.utils.Auth._

trait KafkaProducerRoute extends ActorSystem with KafkaProducerService {

  val kafkaRoutes: HttpRequest => HttpResponse = {
    case req@HttpRequest(GET, Uri.Path("/produce"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => {
          valid(req)
          upgrade.handleMessages(kafkaProducerService)
        }
        case None => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }
}
