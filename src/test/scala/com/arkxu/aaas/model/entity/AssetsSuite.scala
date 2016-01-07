package com.arkxu.aaas.model.entity

import java.io.{File}
import java.nio.ByteBuffer

import com.datastax.driver.core.utils.UUIDs
import com.arkxu.aaas.model.operation.AssetsDataOperation
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by fangxu on 12/5/15.
  */
@RunWith(classOf[JUnitRunner])
class AssetsSuite extends FunSuite with AssetsDataOperation {
  test("create an asset") {
    model.createTables
    val uuid = UUIDs.timeBased()
    val path = List("var", "log")
    val imgFile = new File(getClass.getResource("/testimg.png").toURI)

    val buffer = FileUtils.readFileToByteArray(imgFile)

    val asset = Asset(uuid, "test1", path.mkString(","), "image/png", DateTime.now(), ByteBuffer.wrap(buffer))
    model.save(asset).onComplete {
      case Success(as) => {
        println(as.all())
      }
    }

    val image = model.get(uuid)
    image.onComplete {
      case Success(asset2) => {
        asset2 match {
          case Some(as) =>
            val outFile = new File(getClass.getResource("/").getFile + "testimgout.png")
            if (!outFile.exists()) {
              outFile.createNewFile()
            }
            FileUtils.writeByteArrayToFile(outFile, as.binary.array())
          case _ => throw new Exception("error")
        }
      }
    }

    model.delete(uuid).onComplete {
      case Success(a) => println(s"deleted$a")
      case Failure(a) => println("failed")
    }
  }
}
