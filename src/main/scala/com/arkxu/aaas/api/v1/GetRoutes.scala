package com.arkxu.aaas.api.v1

import java.io.File

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.image.ImageReadMsg
import com.arkxu.aaas.model.operation.AssetsDataOperation
import com.sksamuel.scrimage.Image
import org.apache.commons.io.IOUtils
import org.json4s.native.Serialization

import scala.concurrent.Future

/**
  * Created by arkxu on 12/23/15.
  */
trait GetRoutes extends BaseRoute with Implicits with AssetsDataOperation {
  val defaultWidth = aaasConfig.getInt("aaas.defaultWidth")
  val defaultHeight = aaasConfig.getInt("aaas.defaultHeight")
  val tmpDir = aaasConfig.getString("aaas.tmpDir")

  val getRoutes =
    get {
      path(JavaUUID ~ RestPath) { (id, rest) =>
        val fileName = id.toString + rest.toString()
        val cacheFile = new File(tmpDir, fileName)
        if (cacheFile.exists()) {
          complete {
            HttpResponse(
              status = StatusCodes.OK,
              entity = HttpEntity(ContentType(MediaTypes.`image/jpeg`), IOUtils.toByteArray(cacheFile.toURI))
            )
          }
        } else {
          val asset = model.get(id)
          onSuccess(asset) {
            case None =>
              complete {
                HttpResponse(StatusCodes.NotFound)
              }
            case Some(as) =>
              val restString = rest.toString()
              val regex = """__(\d*)(\w)(\d*).*""".r
              val imgFuture: Future[Image] = restString match {
                case regex(width, m, height) =>
                  (imageProcessActor ? ImageReadMsg(m, width.toInt, height.toInt, as.binary.array(),
                    s"$id$rest")).mapTo[Image]

                case _ =>
                  (imageProcessActor ? ImageReadMsg("z", defaultWidth, defaultHeight, as.binary.array(),
                    s"$id$rest")).mapTo[Image]
              }

              onSuccess(imgFuture) {
                case img =>
                  complete {
                    HttpResponse(
                      status = StatusCodes.OK,
                      entity = HttpEntity(ContentType(MediaTypes.`image/jpeg`), img.bytes)
                    )
                  }
              }
          }
        }
      } ~ path(Segments ~ Slash.?) { segments =>
        onSuccess(model.findByPath(segments)) {
          case assetsByPaths => {
            complete {
              HttpResponse(
                status = StatusCodes.OK,
                entity = HttpEntity(ContentType(MediaTypes.`application/json`),
                  Serialization.write(assetsByPaths))
              )
            }
          }
        }
      }
    }
}
