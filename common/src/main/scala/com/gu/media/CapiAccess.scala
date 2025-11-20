package com.gu.media


import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProvider, AwsCredentialsProviderChain, ProfileCredentialsProvider}
import com.gu.contentapi.client.IAMEncoder
import com.squareup.okhttp.{Headers, OkHttpClient, Request}
import com.typesafe.config.Config
import play.api.libs.json.{JsValue, Json}
import software.amazon.awssdk.http.{SdkHttpFullRequest, SdkHttpMethod}
import software.amazon.awssdk.http.auth.aws.signer.{AwsV4FamilyHttpSigner, AwsV4HttpSigner}
import software.amazon.awssdk.http.auth.spi.signer.SignRequest
import software.amazon.awssdk.identity.spi.AwsCredentialsIdentity
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

import collection.JavaConverters._

trait CapiAccess { this: Settings =>
  def previewCapiIAMUrl = getMandatoryString("capi.previewIAMUrl")
  def previewCapiRole = getMandatoryString("capi.previewRole")
  def liveCapiUrl = getMandatoryString("capi.liveUrl")
  def liveCapiApiKey = getMandatoryString("capi.liveApiKey")

  val stsClient = StsClient.builder().build();
  val capiPreviewCredentials: AwsCredentialsProvider = {
    AwsCredentialsProviderChain.builder()
      .credentialsProviders(
        ProfileCredentialsProvider.builder()
          .profileName("capi")
          .build(),

        StsAssumeRoleCredentialsProvider.builder()
          .stsClient(stsClient)
          .refreshRequest(AssumeRoleRequest.builder().roleArn(previewCapiRole).roleSessionName("capi").build())
          .build()
      )
      .build();
  }
  def creds = capiPreviewCredentials.resolveCredentials()

  val awsRegion = "eu-west-1"
  val serviceName = "media-atom-maker"

  val signer =  AwsV4HttpSigner.create()

  def addIAMHeaders(headers: Map[String, String], uri: URI): Map[String, String] = {

    val queryParams: Map[String, java.util.List[String]] =
      Option(uri.getQuery)
        .map(_.split("&").toList.flatMap { s =>
          s.split("=").toList match {
            case k :: v :: Nil => Some(k -> java.util.Collections.singletonList(v))
            case _ => None
          }
        }.toMap)
        .getOrElse(Map.empty)

    val unsignedRequest: SdkHttpFullRequest = {
      //api-gateway will break the compressed json response if we don't supply an accept header
      val headersWithAccept =
        if (headers.contains("accept") || headers.contains("Accept")) headers
        else headers + ("accept" -> "application/json")

      val reqBuilder = SdkHttpFullRequest.builder()
        .method(SdkHttpMethod.GET)
        .uri(new java.net.URI(s"${uri.getScheme}://${uri.getHost}"))
        .encodedPath(uri.getPath)

      headersWithAccept.foreach { case (k, v) => reqBuilder.putHeader(k, v) }
      queryParams.foreach { case (k, v) => reqBuilder.putRawQueryParameter(k, v) }

      reqBuilder.build()
    }

    val signedRequest = signer.sign { r: SignRequest.Builder[AwsCredentialsIdentity] =>
      r.identity(creds)
        .request(unsignedRequest)
        .putProperty(AwsV4HttpSigner.REGION_NAME, awsRegion)
        .putProperty(AwsV4FamilyHttpSigner.SERVICE_SIGNING_NAME, serviceName)
    }

    signedRequest.request().headers().asScala
      .map { case (k, v) => k -> v.asScala.headOption.getOrElse("") }
      .toMap
  }


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
      else addIAMHeaders(Map.empty[String, String], uri)

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
