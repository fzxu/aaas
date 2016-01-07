package com.arkxu.aaas.model.operation

import com.arkxu.aaas.model.connector.CassandraConnector
import com.arkxu.aaas.model.entity.{Asset, AssetByPath, AssetByPaths, Assets}
import com.websudos.phantom.dsl._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by arkxu on 12/5/15.
  */
trait AssetsDataOperation extends CassandraConnector {

  object model {
    val assetModel = new Assets
    val assetByPathModel = new AssetByPaths

    def createTables = {
      Await.ready(assetModel.create.ifNotExists().future(), 5.seconds)
      Await.ready(assetByPathModel.create.ifNotExists().future(), 5.seconds)
    }

    def save(asset: Asset): Future[ResultSet] = {
      assetModel.insert.value(_.id, asset.id).value(_.name, asset.name)
        .value(_.path, asset.path)
        .value(_.contentType, asset.contentType)
        .value(_.createdAt, asset.createdAt)
        .value(_.binary, asset.binary)
        .future().flatMap {
        _ => {
          assetByPathModel.insert.value(_.path, asset.path)
            .value(_.id, asset.id).value(_.name, asset.name).future()
        }
      }
    }

    def get(id: UUID): Future[Option[Asset]] = {
      assetModel.select.where(_.id eqs id).one()
    }

    def findByPath(segments: List[String]): Future[List[AssetByPath]] = {
      assetByPathModel.select.where(_.path eqs segments.mkString(",")).fetch()
    }

    def delete(id: UUID): Future[ResultSet] = {
      val as = get(id).map {
        case optionAS =>
          optionAS match {
            case Some(a) => a
            case None => throw new Exception("Can not find asset with UUID: " + id)
          }
      }

      as.flatMap {
        case asset =>
          assetModel.delete
            .where(_.id eqs id).future().flatMap {
            _ => {
              assetByPathModel.delete.where(_.path eqs asset.path).and(_.id eqs id).future()
            }
          }
      }
    }
  }

}
