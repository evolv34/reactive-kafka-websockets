package com.kafka.akka.ws

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.kafka.akka.utils.Auth._


object AkkaStreams extends App {
  implicit val streamsSystem = ActorSystem("streams-system")
  implicit val materializer = ActorMaterializer()

  val kafkaMessageStreamService =
    Flow[Message]
      .mapConcat {
        case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          Nil
      }

  val requestHandler: HttpRequest => HttpResponse = {
    case req@HttpRequest(GET, Uri.Path("/kafka"), _, _, _) =>
      req.header[UpgradeToWebSocket] match {
        case Some(upgrade) => {
          valid(req)
          println("Connected Successfully")
          upgrade.handleMessages(kafkaMessageStreamService)
        }
        case None => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }

  val bindingFuture = Http().bindAndHandleSync(requestHandler, "localhost", 9093)
}