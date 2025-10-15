package com.gu.media.util

import java.net.URI

import com.gu.hmac.HMACHeaders
import com.gu.media.Settings
import com.gu.media.aws.HMACSettings
import com.squareup.okhttp.{MediaType, OkHttpClient, Request, RequestBody}
import play.api.libs.json.{JsValue, Json}

class HMACClient(
    serviceName: String,
    config: Settings with HMACSettings
) extends HMACHeaders {
  override def secret: String = config.sharedSecret

  private val httpClient = new OkHttpClient()

  private def baseRequest(uri: URI) = {
    val hmacToken = createHMACHeaderValues(uri)

    new Request.Builder()
      .url(uri.toURL)
      .addHeader("X-Gu-Tools-HMAC-Date", hmacToken.date)
      .addHeader("X-Gu-Tools-HMAC-Token", hmacToken.token)
      .addHeader("X-Gu-Tools-Service-Name", serviceName)
  }

  private def makeRequest(request: Request) = {
    val response = httpClient.newCall(request).execute()
    Json.parse(response.body().byteStream())
  }

  def get(uri: URI) = {
    val req = baseRequest(uri)
      .build()

    makeRequest(req)
  }

  def put(uri: URI): JsValue = {
    val emptyBody = RequestBody.create(
      MediaType.parse("application/json"),
      Array.emptyByteArray
    )
    put(uri, emptyBody)
  }

  def put(uri: URI, requestBody: RequestBody) = {
    val req = baseRequest(uri)
      .put(requestBody)
      .build()

    makeRequest(req)
  }
}
