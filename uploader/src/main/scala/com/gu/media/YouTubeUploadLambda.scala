package com.gu.media

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.gu.media.aws.{DynamoAccess, S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload.{DynamoUploadsTable, Upload, UploadPart, UploadPartKey}
import com.gu.media.youtube.YouTubeAccess
import com.squareup.okhttp._

import scala.collection.JavaConverters._

class YouTubeUploadLambda extends RequestHandler[KinesisEvent, Unit]
  with LambdaBase
  with S3Access
  with UploadAccess
  with DynamoAccess
  with YouTubeAccess
  with YouTubeVideoUpload
  with HmacRequestSupport
  with Logging {

  private val table = new DynamoUploadsTable(this)
  private val domain = sys.env.get("DOMAIN")

  private val http = new OkHttpClient()
  private val appJson = MediaType.parse("application/json")

  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    for {
      key <- readKey(input)
      (upload, part) <- readUploadAndPart(key)

      total = upload.parts.last.end
      uploadUri = getYTUploadUrl(upload)
    } yield {
      log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

      uploadPart(uploadUri, part.key, part.start, part.end, total, (_: Long) => {}).foreach { videoId =>
        log.info(s"Successful upload ${upload.id}. YouTube ID: $videoId")

        addAsset(upload.metadata.atomId, videoId)
        table.delete(upload.id)
      }
    }
  }

  private def getYTUploadUrl(upload: Upload): String = upload.youTube.upload.getOrElse {
    log.info(s"Starting YouTube upload for ${upload.id} [${upload.metadata.title} - ${upload.youTube.channel}]")

    val url = startUpload(upload.metadata.title, upload.youTube.channel, upload.id, upload.parts.last.end)
    val updated = upload.copy(youTube = upload.youTube.copy(upload = Some(url)))

    table.put(updated)
    url
  }

  private def addAsset(atomId: String, videoId: String): Unit = {
    val actuallyPerformRequest = stage != "DEV" && domain.nonEmpty

    val origin = s"${domain.getOrElse("https://video.local.dev-gutools.co.uk")}"
    val uri = s"$origin//api2/atoms/:id/assets"
    val hmacHeaders = generateHmacHeaders(uri)

    val videoUri = s"https://www.youtube.com/watch?v=$videoId"
    val body = s"""{"uri": "$videoUri"}"""

    if(actuallyPerformRequest) {
      val request = new Request.Builder()
        .url(uri)
        .headers(Headers.of(hmacHeaders.asJava))
        .post(RequestBody.create(appJson, body))
        .build()

      val response = http.newCall(request).execute()
      if(response.code() != 200) {
        log.error(s"Unexpected response adding asset ${response.code()}")
        log.error(s"uri=$uri body=$body responseBody=${response.body().string()}")
        log.error(s"atomId=$atomId youTubeId=$videoId")
      }
    } else {
      log.info(s"Add asset: POST $uri $body $hmacHeaders")
    }
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
