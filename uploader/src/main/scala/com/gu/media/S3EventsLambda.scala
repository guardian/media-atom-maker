package com.gu.media

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.aws.{AwsAccess, CrossAccountAccess, DynamoAccess, KinesisAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.{DynamoUploadsTable, UploadPartKey}

import scala.collection.JavaConverters._

class S3EventsLambda extends RequestHandler[S3Event, Unit]
  with LambdaBase
  with Logging
  with AwsAccess with CrossAccountAccess
  with DynamoAccess
  with KinesisAccess {

  val table = new DynamoUploadsTable(this)

  override def handleRequest(input: S3Event, context: Context): Unit = {
    getKeys(input).foreach { key =>
      addToStream(key)
      updateTable(key)
    }
  }

  def getKeys(input: S3Event): List[UploadPartKey] = {
    val records = input.getRecords.asScala.toList
    val keys = records.map(_.getS3.getObject.getKey)

    keys.flatMap {
      case UploadPartKey(folder, id, part) =>
        log.info(s"Chunk complete. folder=$folder id=$id part=$part")

        val key = UploadPartKey(folder, id, part)
        Some(key)

      case other =>
        log.error(s"Unknown part key format: $other")
        None
    }
  }

  def updateTable(key: UploadPartKey): Unit = {
    table.get(key.id) match {
      case Some(before) =>
        val after = before.withPart(key.part, { part =>
          part.copy(uploadedToS3 = part.end - part.start)
        })

        table.put(after)

      case None =>
        log.error(s"Trigger $key for unknown upload id: ${key.id}")
    }
  }

  def addToStream(key: UploadPartKey): Unit = {
    val bytes = ByteBuffer.wrap(key.toString.getBytes(StandardCharsets.UTF_8))
    kinesisClient.putRecord(youTubeUploadsStreamName, bytes, key.id)
  }
}
