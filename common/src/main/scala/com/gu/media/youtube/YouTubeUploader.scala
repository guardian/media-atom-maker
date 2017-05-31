package com.gu.media.youtube

import java.io.InputStream

import com.gu.media.aws.S3Access
import com.gu.media.logging.Logging
import com.gu.media.upload.S3UploadActions
import com.gu.media.upload.model.{Upload, UploadPart}
import com.gu.media.util.InputStreamRequestBody
import com.gu.media.youtube.YouTubeUploader.{MoveToNextChunk, UploadError, VideoFullyUpload}
import com.squareup.okhttp.{MediaType, OkHttpClient, Request, RequestBody}
import play.api.libs.json.{JsObject, Json}

class YouTubeUploader(youTube: YouTubeAccess, s3: S3UploadActions) extends Logging {
  private val JSON = MediaType.parse("application/json; charset=utf-8")
  private val VIDEO = MediaType.parse("video/*")

  private val http = new OkHttpClient()

  def startUpload(title: String, channel: String, id: String, size: Long): String = {
    val contentOwnerParams = s"onBehalfOfContentOwner=${youTube.contentOwner}&onBehalfOfContentOwnerChannel=$channel"
    val params = s"uploadType=resumable&part=snippet,statistics,status&$contentOwnerParams"
    val endpoint = s"https://www.googleapis.com/upload/youtube/v3/videos?$params"

    val videoTitle = s"$title-$id"
    val description = s"Uploaded by the media-atom-maker. Pending publishing"

    val json =
      s"""
         | {
         |    "snippet": {
         |      "title": "$videoTitle",
         |      "description": "$description"
         |    },
         |    "status": {
         |      "privacyStatus": "private"
         |    },
         |    "onBehalfOfContentOwner": "${youTube.contentOwner}",
         |    "onBehalfOfContentOwnerChannel": "$channel"
         | }
       """.stripMargin

    val body = RequestBody.create(JSON, json)
    val request = new Request.Builder()
      .url(endpoint)
      .addHeader("Authorization", "Bearer " + youTube.accessToken())
      .addHeader("X-Upload-Content-Length", size.toString)
      .addHeader("X-Upload-Content-Type", "video/*")
      .post(body)
      .build()

    val response = http.newCall(request).execute()
    response.header("Location")
  }

  def uploadPart(upload: Upload, part: UploadPart, uploadUri: String): Upload = {
    log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

    val UploadPart(key, start, end) = part
    val total = upload.parts.last.end

    val updated = s3.getObjectInput(upload.metadata.bucket, key.toString) match {
      case Some(input) =>
        uploadChunk(uploadUri, input, start, end, total) match {
          case VideoFullyUpload(videoId) =>
            upload.copy(metadata = upload.metadata.copy(youTubeId = Some(videoId)))

          case MoveToNextChunk if part == upload.parts.last =>
            log.error("YouTube did not provide a video id. The asset cannot be added")
            upload

          case MoveToNextChunk =>
            upload

          case UploadError(error) =>
            log.error(error)
            upload
        }

      case None =>
        log.error(s"Unable to upload ${part.key} since it has been deleted from S3")
        upload
    }

    updated.copy(progress = upload.progress.copy(uploadedToYouTube = part.end))
  }

  private def uploadChunk(uri: String, input: InputStream, start: Long, end: Long, total: Long): YouTubeUploader.Result = {
    val size = end - start
    // end index is inclusive in direct contradiction to programming history (and my end variable)
    val range = s"$start-${start + size - 1}"

    val body = new InputStreamRequestBody(VIDEO, input, size)

    val request = new Request.Builder()
      .url(uri)
      .addHeader("Authorization", "Bearer " + youTube.accessToken())
      .addHeader("Content-Length", size.toString)
      .addHeader(s"Content-Range", s"bytes $range/$total")
      .post(body)
      .build()

    val response = http.newCall(request).execute()
    parseResult(response.body().string())
  }

  private def parseResult(result: String): YouTubeUploader.Result = {
    if(result.isEmpty) {
      MoveToNextChunk
    } else {
      val json = Json.parse(result)

      ((json \ "id").asOpt[String], (json \ "error").asOpt[JsObject]) match {
        case (_, Some(error)) =>
          val code = (error \ "code").as[Int]
          val message = (error \ "message").as[String]

          UploadError(s"YouTube upload error $code: $message")

        case (Some(id), None) =>
          VideoFullyUpload(id)

        case (None, None) =>
          UploadError(s"Unable to parse YouTube response $result")
      }
    }
  }
}

object YouTubeUploader {
  sealed trait Result
  case object MoveToNextChunk extends Result
  case class VideoFullyUpload(videoId: String) extends Result
  case class UploadError(error: String) extends Result

  def apply(aws: S3Access, youTube: YouTubeAccess): YouTubeUploader = {
    new YouTubeUploader(youTube, new S3UploadActions(aws.s3Client))
  }
}
