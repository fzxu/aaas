package com.arkxu.aaas.api.v1

import java.util.UUID

import org.joda.time.DateTime

/**
  * Created by arkxu on 12/5/15.
  */
case class AssetRsc(id: UUID, name: String, contentType: String, path: Seq[String], createdAt: DateTime)


