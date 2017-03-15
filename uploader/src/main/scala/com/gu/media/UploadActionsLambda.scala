package com.gu.media

import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.aws.{DynamoAccess, S3Access, UploadAccess}
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.media.youtube.YouTubeAccess
import com.squareup.okhttp._
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class UploadActionsLambda extends RequestHandler[KinesisEvent, Unit]
  with LambdaBase
  with S3Access
  with UploadAccess
  with DynamoAccess
  with YouTubeAccess
  with YouTubeVideoUpload
  with HmacRequestSupport
  with Logging {

  private val table = new UploadsTable(this)
  private val domain = sys.env.get("DOMAIN")

  private val http = new OkHttpClient()
  private val appJson = MediaType.parse("application/json")

  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    readAction(input).foreach {
      case UploadPartToYouTube(uploadId, key) =>
        // TODO: upload to YouTube
        log.info(s"$key uploaded for $uploadId")

      case DeleteParts(uploadId, partsToDelete) =>
        deleteParts(partsToDelete)
    }
  }

  private def uploadToYouTube(uploadId: String, partKey: String): Unit = {
    getUploadAndPart(uploadId, partKey) match {
      case Some((upload, part)) =>
        val total = upload.parts.last.end
        val uploadUri = getYTUploadUrl(upload)

        log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

        uploadPart(uploadUri, part.key, part.start, part.end, total, (_: Long) => {}).foreach { videoId =>
          log.info(s"Successful upload ${upload.id}. YouTube ID: $videoId")

          addAsset(upload.metadata.atomId, videoId)
          table.delete(upload.id)
        }

      case _ =>
        log.error(s"Unknown upload id $uploadId or part $partKey")
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

  private def getUploadAndPart(uploadId: String, partKey: String): Option[(Upload, UploadPart)] = {
    for {
      upload <- table.get(uploadId)
      part <- upload.parts.find(_.key == partKey)
    } yield (upload, part)
  }
}
