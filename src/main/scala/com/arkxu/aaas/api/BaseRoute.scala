package com.arkxu.aaas.api

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.util.Timeout
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.image.ImgeProcessActor

/**
  * Created by fangxu on 1/9/16.
  */
trait BaseRoute extends Implicits {
  val imageProcessActor = system.actorOf(Props[ImgeProcessActor], "imageProcessor")
  val timeoutConf = aaasConfig.getInt("aaas.timeout")
  implicit val timeout = Timeout(timeoutConf, TimeUnit.SECONDS)
}
