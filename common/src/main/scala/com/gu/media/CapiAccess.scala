package com.gu.media

import java.io.IOException
import java.net.URLEncoder
import java.net.URI
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

  private def getUrl(path: String, qs: Map[String, Seq[String]], queryLive: Boolean): URI = {
    val capiDomain = if (queryLive) liveCapiUrl else previewCapiUrl
    val queryString = qs.map(pair => {
      val key = pair._1
      val value = URLEncoder.encode(pair._2.mkString(","), "UTF-8")
      s"$key=$value"
    }).mkString("&")

    URI.create(s"$capiDomain/$path?$queryString")
  }

  private def getAllowedResponseCodes(queryLive: Boolean): List[Int] = {
    if (queryLive) List(200, 404)
    else List(200)
  }

  def capiQuery(path: String, qs: Map[String, String], queryLive: Boolean = false): JsValue = {
    val query: Map[String, Seq[String]] = qs.map(x => (x._1, Seq(x._2)))
    complexCapiQuery(path, query, queryLive)
  }

  def complexCapiQuery(path: String, qs: Map[String, Seq[String]], queryLive: Boolean = false): JsValue = {
    val uri = getUrl(path, qs, queryLive)

    val req = new Request.Builder()
      .url(uri.toURL)
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
