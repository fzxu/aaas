package com.arkxu.aaas.image

import java.io.File
import java.nio.ByteBuffer

import akka.actor.{Actor, Props}
import com.arkxu.aaas.Implicits
import com.arkxu.aaas.api.AssetRsc
import com.arkxu.aaas.model.entity.Asset
import com.arkxu.aaas.model.operation.AssetsDataOperation
import com.datastax.driver.core.utils.UUIDs
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime


/**
  * Created by fangxu on 1/9/16.
  */

// Resize and Save the image to DB
case class ImageStoreMsg(filename: String, path: String, bytes: Array[Byte])

// Get and resize the image, cache to file system
case class ImageReadMsg(mode: String, width: Int, height: Int, bytes: Array[Byte], filename: String)

// Cache to file system
case class FileCacheMsg(filename: String, bytes: Array[Byte])

/**
  * Resize the image based on the requests
  * and save to the DB
  */
class ImgeProcessActor extends Actor with AssetsDataOperation with Implicits {
  val storeWidth = aaasConfig.getInt("aaas.storeWidth")
  val storeHeight = aaasConfig.getInt("aaas.storeHeight")
  val fileCacheActor = context.actorOf(Props[CacheImageActor], "imageFileCache")

  override def receive: Receive = {
    case imgStoreMsg: ImageStoreMsg =>
      val storeQuality = aaasConfig.getInt("aaas.storeQuality")
      implicit val write = JpegWriter.apply(storeQuality, false)

      val id = UUIDs.timeBased()
      val filename = imgStoreMsg.filename
      val path = imgStoreMsg.path
      val createdAt = DateTime.now()
      val img = Image(imgStoreMsg.bytes).max(storeWidth, storeHeight)

      val asset = Asset(id, filename, path, "image/jpeg", createdAt, ByteBuffer.wrap(img.bytes))
      model.save(asset).onFailure {
        case err => log.error(err, s"Can not save image: $imgStoreMsg.filename")
      }

      sender() ! AssetRsc(id, filename, "image/jpeg", path.split(","), createdAt)
    case imgFileCacheMsg: ImageReadMsg =>
      val readQuality = aaasConfig.getInt("aaas.readQuality")
      implicit val write = JpegWriter.apply(readQuality, false)

      imgFileCacheMsg.mode match {
        case "z" =>
          val img = Image(imgFileCacheMsg.bytes).max(imgFileCacheMsg.width, imgFileCacheMsg.height)
          fileCacheActor.tell(FileCacheMsg(imgFileCacheMsg.filename, img.bytes), self)
          sender() ! img
        case "x" =>
          var width = imgFileCacheMsg.width
          var height = imgFileCacheMsg.height

          if (width == 0) {
            width = height
          }
          if (height == 0) {
            height = width
          }

          if (width != 0 && height != 0) {
            val img = Image(imgFileCacheMsg.bytes).cover(width, height)
            fileCacheActor.tell(FileCacheMsg(imgFileCacheMsg.filename, img.bytes), self)
            sender() ! img
          }
      }
    case _ =>
      log.warning("Don't recognize the message")
  }
}

/**
  * Store the resized image to a cache directory for future GET
  */
class CacheImageActor extends Actor with Implicits {
  val tmpDir = aaasConfig.getString("aaas.tmpDir")

  def receive = {
    case fileCacheMsg: FileCacheMsg =>
      val out = new File(tmpDir, fileCacheMsg.filename)
      FileUtils.writeByteArrayToFile(out, fileCacheMsg.bytes)
    case _ =>
      log.warning("Don't recognize the message")
  }
}
