package com.arkxu.aaas.image

import java.io.File

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by arkxu on 12/18/15.
  */
object ImageOp {
  val aaasConfig = ConfigFactory.load()
  val tmpDir = aaasConfig.getString("aaas.tmpDir")
  val readQuality = aaasConfig.getInt("aaas.readQuality")
  implicit val write = JpegWriter.apply(readQuality, false)

  def withCache(binary: Array[Byte], width: Int, height: Int, cacheFileName: String): Image = {
    val img = Image(binary).max(width, height)
    Future {
      val out = new File(tmpDir, cacheFileName)
      img.output(out)
    }
    img
  }

  def resizeWithCache(binary: Array[Byte], resizeMode: String, width: Int, height: Int, cacheFileName: String): Image = {
    val img = resizeMode match {
      case "z" => Image(binary).max(width.toInt, height.toInt)
      case "x" => {
        val w = width.toInt
        val h = height.toInt
        if (w == 0) {
          Image(binary).cover(h, h)
        } else if (h == 0) {
          Image(binary).cover(w, w)
        } else {
          Image(binary).cover(w, h)
        }
      }
    }

    Future {
      val out = new File(tmpDir, cacheFileName)
      img.output(out)
    }
    img
  }
}
