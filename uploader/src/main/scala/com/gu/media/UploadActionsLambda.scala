package com.gu.media

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.aws.{DynamoAccess, S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.UploadAction
import com.gu.media.youtube.{YouTubeAccess, YouTubeUploader}
import play.api.libs.json.{JsError, JsSuccess, Json}

class UploadActionsLambda extends RequestHandler[KinesisEvent, Unit]
  with LambdaBase
  with S3Access
  with UploadAccess
  with DynamoAccess
  with YouTubeAccess
  with HmacRequestSupport
  with Logging {

  val store = new UploadsDataStore(this)
  val uploader = new YouTubeUploader(this, this)
  val handler = new LambdaActionHandler(store, this, uploader)

  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    readAction(input).foreach(handler.handle)
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
}
