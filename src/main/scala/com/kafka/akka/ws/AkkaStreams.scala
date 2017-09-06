package com.kafka.akka.ws

import akka.http.scaladsl.Http
import com.kafka.akka.routes.Routes


object AkkaStreams extends App with Routes {
  val bindingFuture = Http().bindAndHandleSync(routes, "localhost", 9093)
}