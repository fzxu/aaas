package com.arkxu.aaas.api.v1

import akka.actor.Props
import akka.util.Timeout
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.image.ImgeProcessActor

import scala.concurrent.duration._
/**
  * Created by fangxu on 1/9/16.
  */
trait BaseRoute extends Implicits{
  val imageProcessActor = system.actorOf(Props[ImgeProcessActor], "imageProcessor")
  implicit val timeout = Timeout(5.seconds)
}
