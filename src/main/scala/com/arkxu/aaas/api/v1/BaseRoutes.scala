package com.arkxu.aaas.api.v1

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer
import com.arkxu.aaas.Marshallers
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats

/**
  * Created by arkxu on 12/23/15.
  */
trait BaseRoutes {
  implicit val system = ActorSystem("aaas")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, getClass)
  implicit val formats = DefaultFormats ++ Marshallers.all

  val aaasConfig = ConfigFactory.load()
}
