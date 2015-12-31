package com.arkxu.aaas.api.v1

import java.nio.ByteBuffer

import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString
import com.datastax.driver.core.utils.UUIDs
import com.arkxu.aaas.auth.BasicAuth
import com.arkxu.aaas.modeling.entity.Asset
import com.arkxu.aaas.modeling.operation.AssetsDataOperation
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.joda.time.DateTime


/**
  * Created by arkxu on 12/23/15.
  */
trait PostRoutes extends BaseRoutes with AssetsDataOperation {
  val storeWidth = aaasConfig.getInt("aaas.storeWidth")
  val storeHeight = aaasConfig.getInt("aaas.storeHeight")
  val uploadParallelism = aaasConfig.getInt("aaas.uploadParallelism")
  val storeQuality = aaasConfig.getInt("aaas.storeQuality")
  implicit val write = JpegWriter.apply(storeQuality, false)

  val postRoutes =
    post {
      path(Segments ~ Slash.?) { segments =>
        authenticateBasic(realm = "aaas realm", BasicAuth.authenticator) { user =>
          entity(as[Multipart.FormData]) { formData =>

            val done = formData.parts.mapAsync(uploadParallelism) {
              case b: BodyPart =>
                b.entity.dataBytes.runFold(ByteString()) { (out, bytes) =>
                  out ++ bytes
                }.map[AssetRsc] { bytes =>
                  b.filename match {
                    case Some(filename) =>
                      val uuid = UUIDs.timeBased()
                      val createdAt = DateTime.now()
                      val img = Image(bytes.toArray).max(storeWidth, storeHeight)
                      val asset = Asset(uuid, filename, segments.mkString(","), "image/jpeg", createdAt,
                        ByteBuffer.wrap(img.bytes))
                      model.save(asset).onFailure {
                        case err => log.error(err, s"Can not save image: $filename")
                      }
                      val assetRsc = AssetRsc(uuid, filename, "image/jpeg", segments, createdAt)
                      assetRsc
                  }
                }
            }.runFold(List[AssetRsc]()) { (out, asset) => out.::(asset) }

            onSuccess(done) { assets =>
              complete {
                HttpResponse(
                  status = StatusCodes.OK,
                  entity = HttpEntity(ContentType(MediaTypes.`application/json`),
                    org.json4s.native.Serialization.write(assets))
                )
              }
            }
          }
        }
      }
    }
}
