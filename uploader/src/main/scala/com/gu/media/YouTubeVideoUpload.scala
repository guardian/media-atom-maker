package com.gu.media

import java.io.InputStream

import com.gu.media.aws.{S3Access, UploadAccess}
import com.gu.media.youtube.YouTubeAccess
import com.squareup.okhttp.{MediaType, OkHttpClient, Request, RequestBody}
import play.api.libs.json.Json

trait YouTubeVideoUpload { this: S3Access with UploadAccess with YouTubeAccess =>
  private val JSON = MediaType.parse("application/json; charset=utf-8")
  private val VIDEO = MediaType.parse("video/*")

  private val http = new OkHttpClient()

  def startUpload(title: String, channel: String, id: String, size: Long): String = {
    val contentOwnerParams = s"onBehalfOfContentOwner=$contentOwner&onBehalfOfContentOwnerChannel=$channel"
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
         |    "onBehalfOfContentOwner": "$contentOwner",
         |    "onBehalfOfContentOwnerChannel": "$channel"
         | }
       """.stripMargin

    val body = RequestBody.create(JSON, json)
    val request = new Request.Builder()
      .url(endpoint)
      .addHeader("Authorization", "Bearer " + accessToken())
      .addHeader("X-Upload-Content-Length", size.toString)
      .addHeader("X-Upload-Content-Type", "video/*")
      .post(body)
      .build()

    val response = http.newCall(request).execute()
    response.header("Location")
  }

  def uploadPart(uri: String, key: String, start: Long, end: Long, total: Long, uploaded: Long => Unit): Option[String] = {
    val input = s3Client.getObject(userUploadBucket, key.toString).getObjectContent
    uploadPart(uri, input, start, end, total, uploaded)
  }

  def uploadPart(uri: String, input: InputStream, start: Long, end: Long, total: Long, uploaded: Long => Unit): Option[String] = {
    val size = end - start
    // end index is inclusive in direct contradiction to programming history (and my end variable)
    val range = s"$start-${start + size - 1}"

    val body = new InputStreamRequestBody(VIDEO, input, size, uploaded)

    val request = new Request.Builder()
      .url(uri)
      .addHeader("Authorization", "Bearer " + accessToken())
      .addHeader("Content-Length", size.toString)
      .addHeader(s"Content-Range", s"bytes $range/$total")
      .post(body)
      .build()

    val response = http.newCall(request).execute()
    val str = response.body().string()

    if(str.nonEmpty) {
      Some((Json.parse(str) \ "id").as[String])
    } else {
      // next part
      None
    }
  }
}
