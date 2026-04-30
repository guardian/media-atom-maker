package com.gu.media.telemetry

import com.gu.hmac.{HMACHeaderValues, HMACHeaders}
import com.gu.media.Settings
import com.gu.media.config.{Prod, Stage}
import com.gu.media.upload.model.Upload
import com.gu.pandahmac.HMACHeaderNames
import com.gu.pandomainauth.model.User
import play.api.Logging
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.WSClient
import software.amazon.awssdk.auth.credentials.{
  AwsCredentialsProvider,
  AwsCredentialsProviderChain,
  InstanceProfileCredentialsProvider,
  ProfileCredentialsProvider
}

import java.net.URI
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import java.net.http.HttpRequest.BodyPublisher
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.util.Try

private case class TelemetryEvent(
    app: String,
    stage: String,
    `type`: String,
    value: Int,
    eventTime: String,
    tags: Map[String, String]
)

private object TelemetryEvent {
  implicit val format: OFormat[TelemetryEvent] = Json.format
}

object SecretsManager {
  val secretsManagerClient = SecretsManagerClient
    .builder()
    .credentialsProvider(
      AwsCredentialsProviderChain
        .builder()
        .credentialsProviders(
          ProfileCredentialsProvider.create("media-service"),
          InstanceProfileCredentialsProvider.create()
        )
        .build()
    )
    .build()

  def getSecret(secretId: String) = {
    val secretValueRequest =
      GetSecretValueRequest.builder.secretId(secretId).build()
    Try(secretsManagerClient.getSecretValue(secretValueRequest))
      .map(_.secretString())
  }
}

class HMACClient(secretArn: String) extends HMACHeaders {
  lazy val secret = "arn:aws:secretsmanager:eu-west-1:563563610310:secret:/CODE/flexible/event-api-lambda/hmacSecret-OVcnV0"
//    SecretsManager.getSecret(secretArn) getOrElse (throw new Exception(
//      s"Could not retrieve $secretArn from secrets manager"
//    ))
  override def createHMACHeaderValues(uri: URI): HMACHeaderValues =
    super.createHMACHeaderValues(uri)
}

class Telemetry(stage: Stage, secretArn: String, httpClient: HttpClient)
    extends Logging {
  private val telemetryUrl =
    if (stage == Prod)
      "https://user-telemetry.gutools.co.uk/event"
    else
      "https://user-telemetry.code.dev-gutools.co.uk/event"

  val hmacClient = new HMACClient(secretArn)

  def createTags(upload: Upload) = {
    Map(
      "id" -> upload.id,
      "parts" -> upload.parts.length.toString,
      "atomId" -> upload.metadata.pluto.atomId
    )
  }

  def sendTelemetryEvent(
      eventType: String,
      tags: Map[String, String],
      app: String = "media-atom-maker"
  ): Unit = {

    val telemetryURI = new URI(telemetryUrl)
    val hmacHeaderValues = hmacClient.createHMACHeaderValues(telemetryURI)

    val parameters = List(
      TelemetryEvent(
        app = app,
        stage = stage.name,
        `type` = eventType,
        value = 1,
        eventTime = ZonedDateTime
          .now(ZoneOffset.UTC)
          .format(DateTimeFormatter.ISO_INSTANT),
        tags = tags
      )
    )
    val body = Json.stringify(Json.toJson(parameters))

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(telemetryUrl))
      .header(HMACHeaderNames.dateKey, hmacHeaderValues.date)
      .header(HMACHeaderNames.hmacKey, hmacHeaderValues.token)
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(body))
      .build();

    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() >= 400) {
      logger.error(
        s"Failed to send telemetry event, got response ${response.statusCode} - ${response.body}"
      )
    } else {
      logger.info(
        s"Sent telemetry event, got response ${response.statusCode} - ${response.body}"
      )
    }
  }

}
