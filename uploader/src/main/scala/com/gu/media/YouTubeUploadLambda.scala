package com.gu.media

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.aws.{DynamoAccess, S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.{DynamoUploadsTable, Upload, UploadPart, UploadPartKey}
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
    for {
      key <- readKey(input)
      (upload, part) <- readUploadAndPart(key)

      total = upload.parts.last.end
      uploadUri = getYTUploadUrl(upload)
    } yield {
      log.info(s"Uploading $uploadUri ${part.key} [${part.start} - ${part.end}]")

      uploadPart(uploadUri, part.key, part.start, part.end, total, (_: Long) => {}).foreach { videoId =>
        log.info(s"Successful upload ${upload.id}. YouTube ID: $videoId")
        table.delete(upload.id)
      }
    }
  }

  private def getYTUploadUrl(upload: Upload): String = upload.youTube.upload.getOrElse {
    log.info(s"Starting YouTube upload for ${upload.id} [${upload.youTube.title} - ${upload.youTube.channel}]")

    val url = startUpload(upload.youTube.title, upload.youTube.channel, upload.id, upload.parts.last.end)
    val updated = upload.copy(youTube = upload.youTube.copy(upload = Some(url)))

    table.put(updated)
    url
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
        None
    }
  }

  private def readUploadAndPart(key: UploadPartKey): Option[(Upload, UploadPart)] = {
    for {
      upload <- table.consistentlyGet(key.id)
      part <- upload.parts.find(_.key == key.toString)
    } yield (upload, part)
  }
}
