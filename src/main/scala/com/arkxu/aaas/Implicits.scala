package com.arkxu.aaas

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.json4s.DefaultFormats

/**
  * Created by fangxu on 1/9/16.
  */
trait Implicits {
  implicit val system = ActorSystem("aaas")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val log: LoggingAdapter = Logging(system, getClass)
  implicit val formats = DefaultFormats ++ Marshallers.all

  val aaasConfig = ConfigFactory.load()
}
