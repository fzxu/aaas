package com.arkxu.aaas.api

import akka.http.scaladsl.server.Directives._

/**
  * Created by arkxu on 12/23/15.
  */
trait Routes extends GetRoutes with PostRoutes with DeleteRoutes {

  val apiRoutes = {
    getRoutes ~ postRoutes ~ deleteRoutes
  }
}
