package com.gu.media

import com.gu.media.aws.{S3Access, UploadAccess}
import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.UploadActionHandler
import com.gu.media.youtube.YouTubeUploader
import com.squareup.okhttp._

import scala.collection.JavaConverters._

class LambdaActionHandler(store: UploadsDataStore, support: LambdaActionHandler.AWS, youTube: YouTubeUploader, addAssets: Boolean)
  extends UploadActionHandler(store, support, youTube) {

  private val domain = support.getString("host").getOrElse("video.local.dev-gutools.co.uk")
  private val http = new OkHttpClient()
  private val appJson = MediaType.parse("application/json")

  override def addAsset(atomId: String, videoId: String): Long = {
    val uri = s"https://$domain/api2/atoms/$atomId/assets"
    val hmacHeaders = support.generateHmacHeaders(uri)

    val videoUri = s"https://www.youtube.com/watch?v=$videoId"
    val body = s"""{"uri": "$videoUri"}"""

    if(addAssets) {
      val request = new Request.Builder()
        .url(uri)
        .headers(Headers.of(hmacHeaders.asJava))
        .post(RequestBody.create(appJson, body))
        .build()

      val response = http.newCall(request).execute()
      if (response.code() != 200) {
        log.error(s"Unexpected response adding asset ${response.code()}")
        log.error(s"uri=$uri body=$body responseBody=${response.body().string()}")
        log.error(s"atomId=$atomId youTubeId=$videoId")
      }

      // TODO MRB: parse out asset
      -1
    } else {
      log.info(s"Add asset: POST $uri $body $hmacHeaders")
      -1
    }
  }
}

object LambdaActionHandler {
  type AWS = Settings with S3Access with UploadAccess with HmacRequestSupport
}
