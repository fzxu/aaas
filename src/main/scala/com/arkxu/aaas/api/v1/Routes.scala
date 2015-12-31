package com.arkxu.aaas.api.v1

import akka.http.scaladsl.server.Directives._

/**
  * Created by arkxu on 12/23/15.
  */
trait Routes extends GetRoutes with PostRoutes with DeleteRoutes{

  val apiRoutes = pathPrefix("v1") {
    getRoutes ~ postRoutes ~ deleteRoutes
  }
}
