package com.arkxu.aaas.api

import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.ByteString
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.auth.BasicAuth
import com.arkxu.aaas.image.ImageStoreMsg
import com.arkxu.aaas.model.operation.AssetsDataOperation


/**
  * Created by arkxu on 12/23/15.
  */

trait PostRoutes extends BaseRoute with Implicits with AssetsDataOperation {
  val uploadParallelism = aaasConfig.getInt("aaas.uploadParallelism")

  val postRoutes =
    post {
      path(Segments ~ Slash.?) { segments =>
        authenticateBasic(realm = "aaas realm", BasicAuth.authenticator) { user =>
          if (segments.length == 0) {
            complete {
              HttpResponse(
                status = StatusCodes.BadRequest
              )
            }
          } else {
            entity(as[Multipart.FormData]) { formData =>

              val done = formData.parts.mapAsync(uploadParallelism) {
                case b: BodyPart =>
                  b.entity.dataBytes.runFold(ByteString()) { (out, bytes) =>
                    out ++ bytes
                  }.flatMap[AssetRsc] { byteString =>
                    b.filename match {
                      case Some(filename) =>
                        (imageProcessActor ? ImageStoreMsg(filename, segments.mkString(","),
                          byteString.toArray)).mapTo[AssetRsc]
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
}
