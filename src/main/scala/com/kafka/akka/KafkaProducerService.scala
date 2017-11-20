package com.kafka.akka

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Flow, Sink}
import com.kafka.akka.messages.WebSocketMessage
import com.kafka.akka.routes.ActorSystem
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import spray.json.JsonParser

trait KafkaProducerService extends ActorSystem {

  val producerSettings =
    ProducerSettings(
      streamsSystem,
      new ByteArraySerializer,
      new StringSerializer)
      .withBootstrapServers("localhost:9092")

  import messages.WebSocketMessageObject._

  val kafkaProducerService =
    Flow[Message]
      .mapConcat {
        case tm: TextMessage => {
          tm
            .textStream
            .map(JsonParser(_).convertTo[WebSocketMessage])
            .map(elem => new ProducerRecord[Array[Byte], String](elem.topic, elem.content))
            .runWith(Producer.plainSink(producerSettings))

          Nil
        }
        case bm: BinaryMessage =>
          bm.dataStream.runWith(Sink.ignore)
          Nil
      }

}
