package telemetry

import com.gu.hmac.{HMACHeaderValues, HMACHeaders}
import com.gu.media.Settings
import com.gu.pandahmac.HMACHeaderNames
import com.gu.pandomainauth.model.User
import config.{Prod, Stage}
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
  lazy val secret =
    SecretsManager.getSecret(secretArn) getOrElse (throw new Exception(
      s"Could not retrieve $secretArn from secrets manager"
    ))
  override def createHMACHeaderValues(uri: URI): HMACHeaderValues =
    super.createHMACHeaderValues(uri)
}

class Telemetry(stage: Stage, secretArn: String, wsClient: WSClient)
    extends Logging {
  private val telemetryUrl =
    if (stage == Prod)
      "https://user-telemetry.gutools.co.uk/event"
    else
      "https://user-telemetry.code.dev-gutools.co.uk/event"

  val hmacClient = new HMACClient(secretArn)

  def sendTelemetryEvent(
      eventType: String,
      tags: Map[String, String],
      app: String = "media-atom-maker"
  )(implicit executionContext: ExecutionContext): Future[Unit] = {

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
    val body = Json.toJson(parameters)

    wsClient
      .url(telemetryUrl)
      .withMethod("POST")
      .addHttpHeaders(
        HMACHeaderNames.dateKey -> hmacHeaderValues.date,
        HMACHeaderNames.hmacKey -> hmacHeaderValues.token
      )
      .withBody(body)
      .execute()
      .map { response =>
        if (response.status >= 400) {
          logger.error(
            s"Failed to send telemetry event, got response ${response.status} - ${response.body}"
          )
        }
        ()
      }
  }

}
