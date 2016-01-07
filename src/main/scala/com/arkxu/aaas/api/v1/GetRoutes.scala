package com.arkxu.aaas.api.v1

import java.io.File

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.arkxu.aaas.image.ImageOp
import com.arkxu.aaas.model.operation.AssetsDataOperation
import org.apache.commons.io.IOUtils
import org.json4s.native.Serialization

/**
  * Created by arkxu on 12/23/15.
  */
trait GetRoutes extends BaseRoutes with AssetsDataOperation {
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
              val img = restString match {
                case regex(width, m, height) =>
                  ImageOp.resizeWithCache(as.binary.array(), m, width.toInt, height.toInt, s"$id$rest")

                case _ =>
                  ImageOp.withCache(as.binary.array(), defaultWidth, defaultHeight, s"$id$rest")
              }
              complete {
                HttpResponse(
                  status = StatusCodes.OK,
                  entity = HttpEntity(ContentType(MediaTypes.`image/jpeg`), img.bytes)
                )
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
