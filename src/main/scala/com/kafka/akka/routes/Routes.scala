package com.kafka.akka.routes

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait Routes extends KafkaRoute {
  lazy val routes = kafkaRoutes
}

trait ActorSystem {
  implicit val streamsSystem = ActorSystem("streams-system")
  implicit val materializer = ActorMaterializer()
}
