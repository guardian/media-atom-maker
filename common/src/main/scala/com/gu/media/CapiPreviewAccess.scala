package com.gu.media

import java.io.IOException
import java.util.concurrent.TimeUnit

import com.squareup.okhttp.{Credentials, OkHttpClient, Request}
import play.api.libs.json.{JsValue, Json}

trait CapiPreviewAccess { this: Settings =>
  def capiPreviewUser = getMandatoryString("capi.previewUser")
  def capiPreviewPassword = getMandatoryString("capi.previewPassword")
  def capiUrl = getMandatoryString("capi.previewUrl")

  private val httpClient = new OkHttpClient()
  httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

  def capiQuery(query: String): JsValue = {
    val url = s"$capiUrl/$query"

    val req = new Request.Builder()
      .url(url)
      .header("Authorization", Credentials.basic(capiPreviewUser, capiPreviewPassword))
      .build

    try {
      val response = httpClient.newCall(req).execute

      if(response.code() != 200)
        throw CapiException(s"CAPI returned status ${response.code()}")

      Json.parse(response.body().byteStream())
    } catch {
      case err: IOException =>
        throw CapiException(err.getMessage, err)
    }
  }
}

case class CapiException(err: String, cause: Throwable = null) extends RuntimeException(err, cause)
