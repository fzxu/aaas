package com.arkxu.aaas.api.v1

import akka.http.scaladsl.model.{StatusCodes, HttpResponse}
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.auth.BasicAuth
import com.arkxu.aaas.model.operation.AssetsDataOperation
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

/**
  * Created by arkxu on 12/24/15.
  */
trait DeleteRoutes extends BaseRoute with AssetsDataOperation with Implicits{
  val deleteRoutes =
    delete {
      authenticateBasic(realm = "aaas realm", BasicAuth.authenticator) { user =>
        path(JavaUUID ~ RestPath) { (id, rest) =>
          onSuccess(model.delete(id)) {
            case _ =>
              complete {
                HttpResponse(status = StatusCodes.OK)
              }
          }
        } ~ {
          path(Segments ~ Slash.?) { segments =>
            val assetByPaths = model.findByPath(segments)
            val done = assetByPaths.flatMap { abps =>
              Future {
                for (abp <- abps) {
                  model.delete(abp.id)
                }
              }
            }

            onSuccess(done) {
              complete {
                HttpResponse(status = StatusCodes.OK)
              }
            }
          }
        }
      }
    }
}
