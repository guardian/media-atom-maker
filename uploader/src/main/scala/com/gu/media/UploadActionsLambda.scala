package com.gu.media

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.aws.{S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload._
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.util.control.NonFatal

class UploadActionsLambda extends RequestHandler[KinesisEvent, Unit]
  with LambdaBase
  with S3Access
  with UploadAccess
  with Logging {

  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    readAction(input).foreach {
      case UploadPartToYouTube(uploadId, key) =>
        // TODO: upload to YouTube
        log.info(s"$key uploaded for $uploadId")

      case DeleteParts(uploadId, partsToDelete) =>
        deleteParts(partsToDelete)
    }
  }

  private def readAction(input: KinesisEvent): Option[UploadAction] = {
    val records = input.getRecords

    if(records.size() > 1) {
      log.error(s"Expected 1 record in each batch, got ${records.size()}. The extra records will be discarded")
    }

    val record = input.getRecords.get(0)
    val data = new String(record.getKinesis.getData.array(), "UTF-8")

    Json.parse(data).validate[UploadAction] match {
      case JsSuccess(action, _) =>
        Some(action)

      case JsError(err) =>
        log.error(s"Unable to parse $data: $err")
        None
    }
  }

  private def deleteParts(partsToDelete: List[String]): Unit = {
    // The complete key will be deleted once it has been ingested by Pluto
    partsToDelete.foreach { part =>
      try {
        log.info(s"Deleting part $part")
        s3Client.deleteObject(userUploadBucket, part)
      } catch {
        case NonFatal(err) =>
          // if we can't delete it, no problem. the bucket policy will remove it in time
          log.warn(s"Unable to delete part $part: $err")
      }
    }
  }
}
