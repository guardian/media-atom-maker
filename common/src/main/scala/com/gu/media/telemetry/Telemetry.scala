package com.gu.media.telemetry

import com.gu.hmac.HMACHeaders
import com.gu.media.config.{Prod, Stage}
import com.gu.pandahmac.HMACHeaderNames
import play.api.Logging
import play.api.libs.json.{JsNumber, JsString, JsValue, Json, OWrites, Writes}
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider

import java.net.URI
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import scala.util.Try

sealed trait TagValue

case class TagString(value: String) extends TagValue
case class TagLong(value: Long) extends TagValue
case class TagInt(value: Int) extends TagValue
private case class TelemetryEvent(
    app: String,
    stage: String,
    `type`: String,
    value: Int,
    eventTime: String,
    tags: Map[String, TagValue]
)

private object TelemetryEvent {
  implicit val tagValueWrite: Writes[TagValue] = new Writes[TagValue] {
    override def writes(tagValue: TagValue): JsValue = {
      tagValue match {
        case TagString(v) => JsString(v)
        case TagLong(v)   => JsNumber(v)
        case TagInt(v)    => JsNumber(v)
      }
    }
  }
  implicit val writes: OWrites[TelemetryEvent] = Json.writes
}

object SecretsManager {
  val secretsManagerClient = SecretsManagerClient
    .builder()
    .credentialsProvider(
      DefaultCredentialsProvider.builder().build()
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
  lazy val secret =
    SecretsManager.getSecret(secretArn) getOrElse (throw new Exception(
      s"Could not retrieve $secretArn from secrets manager"
    ))
}

class Telemetry(stage: Stage, secretArn: String, httpClient: HttpClient)
    extends Logging {
  private val telemetryUrl =
    if (stage == Prod)
      "https://user-telemetry.gutools.co.uk/event"
    else
      "https://user-telemetry.code.dev-gutools.co.uk/event"

  val hmacClient = new HMACClient(secretArn)

  def sendTelemetryEvent(
      eventType: String,
      tags: Map[String, TagValue],
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
