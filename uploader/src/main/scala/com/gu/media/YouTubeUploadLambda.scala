package com.gu.media

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.gu.media.aws.{DynamoAccess, S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.{DynamoUploadsTable, Upload, UploadPartKey}
import com.gu.media.youtube.YouTubeAccess

class YouTubeUploadLambda extends RequestHandler[KinesisEvent, Unit]
  with LambdaBase
  with S3Access
  with UploadAccess
  with DynamoAccess
  with YouTubeAccess
  with YouTubeVideoUpload
  with Logging {

  val table = new DynamoUploadsTable(this)

  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    readKey(input).foreach { part =>
      table.get(part.id) match {
        case Some(upload) =>


        case None =>
          log.error(s"Unknown upload id ${part.id} [$part]")
      }
    }
  }

  private def getMultipartUrl(upload: Upload): String = upload.youTube.multipartUpload.getOrElse {
    ???
  }

  private def readKey(input: KinesisEvent): Option[UploadPartKey] = {
    val records = input.getRecords

    if(records.size() > 1) {
      log.error(s"Expected 1 record in each batch, got ${records.size()}. The extra records will be discarded")
    }

    val record = input.getRecords.get(0)
    val data = new String(record.getKinesis.getData.array(), "UTF-8")

    data match {
      case UploadPartKey(folder, id, part) =>
        Some(UploadPartKey(folder, id, part))

      case other =>
        log.error(s"Unknown format for part key $other")
        None
    }
  }
}
