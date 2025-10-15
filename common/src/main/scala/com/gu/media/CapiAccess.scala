package com.gu.media

import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{
  AWSCredentialsProvider,
  AWSCredentialsProviderChain,
  STSAssumeRoleSessionCredentialsProvider
}
import com.gu.contentapi.client.{IAMSigner, IAMEncoder}
import com.squareup.okhttp.{Headers, OkHttpClient, Request}
import com.typesafe.config.Config
import play.api.libs.json.{JsValue, Json}

import collection.JavaConverters._

trait CapiAccess { this: Settings =>
  def previewCapiIAMUrl = getMandatoryString("capi.previewIAMUrl")
  def previewCapiRole = getMandatoryString("capi.previewRole")
  def liveCapiUrl = getMandatoryString("capi.liveUrl")
  def liveCapiApiKey = getMandatoryString("capi.liveApiKey")

  private val capiPreviewCredentials: AWSCredentialsProvider = {
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("capi"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(
        previewCapiRole,
        "capi"
      ).build()
    )
  }

  private val signer = new IAMSigner(
    capiPreviewCredentials,
    sys.env.getOrElse("REGION", "eu-west-1")
  )

  private val httpClient = new OkHttpClient()
  httpClient.setConnectTimeout(5, TimeUnit.SECONDS)

  private def getUrl(
      path: String,
      qs: Map[String, Seq[String]],
      queryLive: Boolean
  ): URI = {
    val capiDomain = if (queryLive) liveCapiUrl else previewCapiIAMUrl
    val queryString = IAMEncoder.encodeParams(
      if (queryLive) {
        qs + ("api-key" -> Seq(liveCapiApiKey))
      } else {
        qs
      }
    )

    URI.create(s"$capiDomain/$path?$queryString")
  }

  private def getAllowedResponseCodes(queryLive: Boolean): List[Int] = {
    if (queryLive) List(200, 404)
    else List(200)
  }

  def capiQuery(
      path: String,
      qs: Map[String, String],
      queryLive: Boolean = false
  ): JsValue = {
    val query: Map[String, Seq[String]] = qs.map(x => (x._1, Seq(x._2)))
    complexCapiQuery(path, query, queryLive)
  }

  def complexCapiQuery(
      path: String,
      qs: Map[String, Seq[String]],
      queryLive: Boolean = false
  ): JsValue = {
    val uri = getUrl(path, qs, queryLive)

    val headers: Map[String, String] =
      if (queryLive) Map.empty
      else signer.addIAMHeaders(Map.empty[String, String], uri)

    val req = new Request.Builder()
      .url(uri.toURL)
      .headers(Headers.of(headers.asJava))
      .build

    try {
      val response = httpClient.newCall(req).execute
      val allowedCodes = getAllowedResponseCodes(queryLive)

      if (!allowedCodes.contains(response.code()))
        throw CapiException(s"CAPI returned status ${response.code()}")

      Json.parse(response.body().byteStream())
    } catch {
      case err: IOException =>
        throw CapiException(err.getMessage, err)
    }
  }
}

class Capi(override val config: Config) extends Settings with CapiAccess
case class CapiException(err: String, cause: Throwable = null)
    extends RuntimeException(err, cause)
