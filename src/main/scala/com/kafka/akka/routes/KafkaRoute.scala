package com.kafka.akka.routes

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.kafka.akka.utils.Auth.valid
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

trait KafkaRoute extends BaseRoute {

  val producerSettings = ProducerSettings(streamsSystem, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val kafkaMessageStreamService =
    Flow[Message]
      .mapConcat {
        case tm: TextMessage => {
          tm
            .textStream
            .map(_.toString)
            .map(elem => new ProducerRecord[Array[Byte], String]("topic1", elem))
            .runWith(Producer.plainSink(producerSettings))

          TextMessage(Source.single("Welcome to WebSocket Boiler plate code, ") ++ tm.textStream) :: Nil
        }
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          Nil
      }

  val kafkaRoutes: HttpRequest => HttpResponse = {
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
}
