package com.arkxu.aaas.modeling.entity

import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.TimeUUIDColumn
import com.websudos.phantom.dsl.{Row, StringColumn}
import com.websudos.phantom.keys.{PrimaryKey, PartitionKey}

/**
  * Created by arkxu on 12/23/15.
  */
case class AssetByPath(path: String, id: UUID)

class AssetByPaths extends CassandraTable[AssetByPaths, AssetByPath] {

  object path extends StringColumn(this) with PartitionKey[String]

  object id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  def fromRow(row: Row): AssetByPath = {
    AssetByPath(
      path(row),
      id(row)
    )
  }
}
