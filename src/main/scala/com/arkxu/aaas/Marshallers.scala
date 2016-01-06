package com.arkxu.aaas

import java.util.UUID

import com.arkxu.aaas.api.v1.AssetRsc
import org.joda.time.DateTime
import org.json4s.JsonAST.{JField, JObject, JString}
import org.json4s.JsonDSL._
import org.json4s.ext.{DateTimeSerializer, UUIDSerializer}
import org.json4s.{CustomSerializer, DefaultFormats, Extraction}


/**
  * Created by arkxu on 12/13/15.
  */
object Marshallers {
  lazy val all = Seq(AssetRscSerializer, UUIDSerializer)
}


case object AssetRscSerializer extends CustomSerializer[AssetRsc](format => ( {
  case JObject(JField("id", id) :: JField("name", JString(name)) :: JField("content_type", JString(contentType))
    :: JField("path", path) :: JField("created_at", createdAt)) =>

    implicit val formats = DefaultFormats ++
      Seq(UUIDSerializer, DateTimeSerializer)

    AssetRsc(Extraction.extract[UUID](id), name, contentType, Extraction.extract[Seq[String]](path),
      Extraction.extract[DateTime](createdAt))
}, {
  case AssetRsc(id, name, contentType, path, createdAt) =>

    implicit val formats = DefaultFormats ++
      Seq(UUIDSerializer, DateTimeSerializer)

    ("id" -> Extraction.decompose(id)) ~ ("name" -> name) ~ ("content_type" -> contentType) ~
      ("path" -> Extraction.decompose(path)) ~ ("created_at" -> Extraction.decompose(createdAt))
}))
