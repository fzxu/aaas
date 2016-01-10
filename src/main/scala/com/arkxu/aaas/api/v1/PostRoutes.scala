package com.arkxu.aaas.api.v1

import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.util.ByteString
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.auth.BasicAuth
import com.arkxu.aaas.image.ImageStoreMsg
import com.arkxu.aaas.model.operation.AssetsDataOperation
import com.datastax.driver.core.utils.UUIDs
import org.joda.time.DateTime


/**
  * Created by arkxu on 12/23/15.
  */

trait PostRoutes extends BaseRoute with Implicits with AssetsDataOperation {
  val uploadParallelism = aaasConfig.getInt("aaas.uploadParallelism")

  val postRoutes =
    post {
      path(Segments ~ Slash.?) { segments =>
        authenticateBasic(realm = "aaas realm", BasicAuth.authenticator) { user =>
          entity(as[Multipart.FormData]) { formData =>

            val done = formData.parts.mapAsync(uploadParallelism) {
              case b: BodyPart =>
                b.entity.dataBytes.runFold(ByteString()) { (out, bytes) =>
                  out ++ bytes
                }.map[AssetRsc] { byteString =>
                  b.filename match {
                    case Some(filename) =>
                      val uuid = UUIDs.timeBased()
                      val createdAt = DateTime.now()
                      imageProcessActor ! ImageStoreMsg(uuid, filename, segments.mkString(","),
                        byteString.toArray)
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
