package com.gu.media

import java.io.IOException
import java.util.concurrent.TimeUnit

import com.squareup.okhttp.{Credentials, OkHttpClient, Request}
import com.typesafe.config.Config
import play.api.libs.json.{JsValue, Json}

trait CapiAccess { this: Settings =>
  def capiPreviewUser = getMandatoryString("capi.previewUser")
  def capiPreviewPassword = getMandatoryString("capi.previewPassword")
  def previewCapiUrl = getMandatoryString("capi.previewUrl")
  def liveCapiUrl = getMandatoryString("capi.liveUrl")

  private val httpClient = new OkHttpClient()
  httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

  private def getUrl(query: String, queryLive: Boolean): String = {
    if (queryLive) s"$liveCapiUrl/$query"
    else s"$previewCapiUrl/$query"
  }

  private def getAllowedResponseCodes(queryLive: Boolean): List[Int] = {
    if (queryLive) List(200, 404)
    else List(200)
  }

  def capiQuery(query: String, queryLive: Boolean = false): JsValue = {
    val url = getUrl(query, queryLive)

    val req = new Request.Builder()
      .url(url)
      .header("Authorization", Credentials.basic(capiPreviewUser, capiPreviewPassword))
      .build

    try {
      val response = httpClient.newCall(req).execute
      val allowedCodes = getAllowedResponseCodes(queryLive)

      if(! allowedCodes.contains(response.code()))
        throw CapiException(s"CAPI returned status ${response.code()}")

      Json.parse(response.body().byteStream())
    } catch {
      case err: IOException =>
        throw CapiException(err.getMessage, err)
    }
  }
}

class Capi(override val config: Config) extends Settings with CapiAccess
case class CapiException(err: String, cause: Throwable = null) extends RuntimeException(err, cause)
