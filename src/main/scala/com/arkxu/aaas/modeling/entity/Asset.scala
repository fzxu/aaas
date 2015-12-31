package com.arkxu.aaas.modeling.entity

import java.nio.ByteBuffer
import java.util.UUID

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import org.joda.time.DateTime


/**
  * Created by arkxu on 12/5/15.
  */
case class Asset(
                  id: UUID,
                  name: String,
                  path: String,
                  contentType: String,
                  createdAt: DateTime,
                  binary: ByteBuffer
                )

class Assets extends CassandraTable[Assets, Asset] {

  object id extends TimeUUIDColumn(this) with PartitionKey[UUID]

  object name extends StringColumn(this)

  object path extends StringColumn(this)

  object contentType extends StringColumn(this)

  object createdAt extends DateTimeColumn(this)

  object binary extends BlobColumn(this)

  def fromRow(row: Row): Asset = {
    Asset(
      id(row),
      name(row),
      path(row),
      contentType(row),
      createdAt(row),
      binary(row)
    )
  }
}
