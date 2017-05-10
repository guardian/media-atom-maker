package com.gu.media

import com.gu.media.aws.AwsAccess.UploaderAccess
import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.UploadActionHandler
import com.gu.media.youtube.YouTubeUploader
import com.squareup.okhttp._
import play.api.libs.json.{JsArray, JsValue, Json}
import scala.collection.JavaConverters._

class LambdaActionHandler(store: UploadsDataStore, plutoStore: PlutoDataStore, aws: UploaderAccess,
                          youTube: YouTubeUploader, hmac: HmacRequestSupport)
  extends UploadActionHandler(store, plutoStore, aws, youTube) {

  private val domain = aws.getString("host").getOrElse("dev")
  private val http = new OkHttpClient()
  private val appJson = MediaType.parse("application/json")

  override def addAsset(atomId: String, videoId: String): Long = {
    val uri = s"https://$domain/api2/atoms/$atomId/assets"
    val hmacHeaders = hmac.generateHmacHeaders(uri)

    val videoUri = s"https://www.youtube.com/watch?v=$videoId"
    val body = s"""{"uri": "$videoUri"}"""

    if(aws.stage == "DEV") {
      log.info(s"Add asset: POST $uri $body $hmacHeaders")
      -1
    } else {
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

        -1
      } else {
        val str = response.body().string()
        val json = Json.parse(str)

        parseAssetVersion(json)
      }
    }
  }

  private def parseAssetVersion(json: JsValue): Long = {
    val assets = (json \ "assets").as[JsArray].value
    val versions = assets.map { asset => (asset \ "version").as[Long] }

    versions.sorted.last
  }
}
