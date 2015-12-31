package com.arkxu.aaas.image

import java.io.{ByteArrayInputStream, File}

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
  val readQuality = aaasConfig.getInt("aaas.readQuality")
  implicit val write = JpegWriter.apply(readQuality, false)

  def withCache(binary: Array[Byte], width: Int, height: Int, cacheFileName: String): Image = {
    val imgIS = new ByteArrayInputStream(binary)
    try {
      val img = Image.fromStream(imgIS).max(width, height)
      Future {
        val out = new File("/tmp/" + cacheFileName)
        img.output(out)
      }
      img
    } finally {
      imgIS.close()
    }
  }

  def resizeWithCache(binary: Array[Byte], resizeMode: String, width: Int, height: Int, cacheFileName: String): Image = {
    val imgIS = new ByteArrayInputStream(binary)
    try {
      val img = resizeMode match {
        case "z" => Image.fromStream(imgIS).max(width.toInt, height.toInt)
        case "x" => {
          val w = width.toInt
          val h = height.toInt
          if (w == 0) {
            Image.fromStream(imgIS).cover(h, h)
          } else if (h == 0) {
            Image.fromStream(imgIS).cover(w, w)
          } else {
            Image.fromStream(imgIS).cover(w, h)
          }
        }
      }

      Future {
        val out = new File("/tmp/" + cacheFileName)
        img.output(out)
      }
      img
    } finally {
      imgIS.close()
    }
  }
}
