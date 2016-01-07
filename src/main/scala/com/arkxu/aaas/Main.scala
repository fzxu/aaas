package com.arkxu.aaas

import akka.http.scaladsl.Http
import com.arkxu.aaas.api.v1.Routes
import com.arkxu.aaas.model.operation.AssetsDataOperation

import scala.util.{Failure, Success}

/**
  * Created by arkxu on 12/5/15.
  */

object Main extends App with Routes {
  val host = aaasConfig.getString("aaas.host")
  val port = aaasConfig.getInt("aaas.port")

  val bindingFuture = Http().bindAndHandle(apiRoutes, host, port)

  bindingFuture.onComplete {
    case Success(b) =>
      log.info(s"Server started on $host:$port")
      sys.addShutdownHook {
        b.unbind()
        log.info("Server stopped")
      }
    case Failure(e) =>
      log.info(s"Cannot start server on $host:$port")
      sys.addShutdownHook {
        log.info("Server stopped")
      }
      sys.exit(1)
  }
}
