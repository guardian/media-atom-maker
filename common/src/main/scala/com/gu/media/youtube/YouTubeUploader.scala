package com.gu.media.youtube

import java.io.InputStream
import com.amazonaws.services.s3.AmazonS3
import com.gu.media.logging.{Logging, YoutubeApiType, YoutubeRequestLogger, YoutubeRequestType}
import com.gu.media.model.{YouTubeAsset}
import com.gu.media.upload.model.{Upload, UploadPart}
import com.gu.media.util.InputStreamRequestBody
import com.gu.media.youtube.YouTubeUploader.{FileDetails, MoveToNextChunk, UploadError, VideoFullyUploaded}
import com.squareup.okhttp.{MediaType, OkHttpClient, Request, RequestBody}
import play.api.libs.json.{JsObject, JsString, Json}

object YouTubeUploader {
  sealed trait Result
  case object MoveToNextChunk extends Result
  object VideoStream {
        implicit val videoStreamFormat = Json.format[VideoStream]
  }

  case class VideoStream(aspectRatio: Double)
    object FileDetails {
      implicit val fileDetailsFormat = Json.format[FileDetails]
    }
  case class FileDetails(videoStreams: List[VideoStream])




  case class VideoFullyUploaded(videoId: String, fileDetails: FileDetails) extends Result
  case class UploadError(error: String) extends Result
}


class YouTubeUploader(youTube: YouTubeAccess, s3: AmazonS3) extends Logging {
  private val JSON = MediaType.parse("application/json; charset=utf-8")
  private val VIDEO = MediaType.parse("video/*")

  private val http = new OkHttpClient()

  def startUpload(title: String, channel: String, id: String, size: Long): String = {
    val contentOwnerParams = s"onBehalfOfContentOwner=${youTube.contentOwner}&onBehalfOfContentOwnerChannel=$channel"
    val params = s"uploadType=resumable&part=snippet,fileDetails,statistics,status&$contentOwnerParams"
    val endpoint = s"https://www.googleapis.com/upload/youtube/v3/videos?$params"

    val videoTitle = s"$title-$id".take(70) // YouTube character limit

    val json = JsObject(Seq(
      "snippet" -> JsObject(Seq(
        "title" -> JsString(videoTitle)
      )),
      "status" -> JsObject(Seq(
        "privacyStatus" -> JsString("unlisted")
      )),
      "onBehalfOfContentOwner" -> JsString(youTube.contentOwner),
      "onBehalfOnContentOwnerChannel" -> JsString(channel)
    ))

    val body = RequestBody.create(JSON, Json.stringify(json))
    log.info(s"endpoint is: ${endpoint}")
    log.info(s"body is: ${Json.stringify(json)}")
    val request = new Request.Builder()
      .url(endpoint)
      .addHeader("Authorization", "Bearer " + youTube.accessToken())
      .addHeader("X-Upload-Content-Length", size.toString)
      .addHeader("X-Upload-Content-Type", "video/*")
      .post(body)
      .build()

    YoutubeRequestLogger.logRequest(YoutubeApiType.UploadApi, YoutubeRequestType.StartVideoUpload)
    val response = http.newCall(request).execute()

    if(response.code() == 200) {
      response.header("Location")
    } else {
      throw new IllegalStateException(s"${response.code()} when starting YouTube upload: ${response.body().string()}")
    }
  }

  def uploadPart(upload: Upload, part: UploadPart, uploadUri: String): Upload = {
    log.info(s"Uploading ${part.key} [${part.start} - ${part.end}]")

    val UploadPart(key, start, end) = part
    val total = upload.parts.last.end

    val input = s3.getObject(upload.metadata.bucket, key).getObjectContent

    uploadChunk(uploadUri, input, start, end, total) match {
      case VideoFullyUploaded(videoId, fileDetails) =>
        val aspectRatio = if (fileDetails.videoStreams(0).aspectRatio == 0.5625) {
          Some("16:9")
        } else {
          None
        }

        upload.copy(
          progress = upload.progress.copy(chunksInYouTube = upload.progress.chunksInYouTube + 1),
          metadata = upload.metadata.copy(asset = Some(YouTubeAsset(videoId, aspectRatio) ))
        )

      case MoveToNextChunk if part == upload.parts.last =>
        throw new IllegalStateException("YouTube did not provide a video id. The asset cannot be added")

      case MoveToNextChunk =>
        upload.copy(progress = upload.progress.copy(
          chunksInYouTube = upload.progress.chunksInYouTube + 1
        ))

      case UploadError(error) =>
        throw new IllegalStateException(error)
    }
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

    YoutubeRequestLogger.logRequest(YoutubeApiType.UploadApi, YoutubeRequestType.UploadVideoChunk)
    val response = http.newCall(request).execute()
    parseResult(response.body().string())
  }

  private def parseResult(result: String): YouTubeUploader.Result = {
    if(result.isEmpty) {
      MoveToNextChunk
    } else {
      val json = Json.parse(result)

      ((json \ "id").asOpt[String],  (json \ "error").asOpt[JsObject]) match {
        case (_, Some(error)) =>
          val code = (error \ "code").as[Int]
          val message = (error \ "message").as[String]

          UploadError(s"YouTube upload error $code: $message")

        case (Some(id), None) =>
          val fileDetails: FileDetails = (json \ "fileDetails").as[FileDetails]
          VideoFullyUploaded(id, fileDetails)

        case (None, None) =>
          UploadError(s"Unable to parse YouTube response $result")
      }
    }
  }
}

