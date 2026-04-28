package telemetry

import com.gu.hmac.{HMACHeaderValues, HMACHeaders}
import com.gu.pandahmac.HMACHeaderNames
import com.gu.pandomainauth.model.User
import config.{Prod, Stage}
import play.api.Logging
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie}
import play.api.mvc.Cookie

import java.net.URI
import java.time.{ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}


private case class TelemetryEvent(
                                   app: String,
                                   stage: String,
                                   `type`: String,
                                   value: Int,
                                   eventTime: String,
                                   tags: Map[String, String],
                                 )

private object TelemetryEvent {
  implicit val format: OFormat[TelemetryEvent] = Json.format
}

class HMACClient extends HMACHeaders {
//  val telemetryHmacRoleArn = dashboardConfig.telemetryHmacRoleArn
//  val telemetryHmacSecretName = dashboardConfig.telemetryHmacSecretName

  lazy val secret = "changeme"

  override def createHMACHeaderValues(uri: URI): HMACHeaderValues =
    super.createHMACHeaderValues(uri)
}

class Telemetry(stage: Stage, wsClient: WSClient) extends Logging {
  private val telemetryUrl =
    if (stage == Prod)
      "https://user-telemetry.gutools.co.uk/event"
    else
      "https://user-telemetry.code.dev-gutools.co.uk/event"

  val hmacClient = new HMACClient()

  def sendTelemetryEvent(eventType: String, tags: Map[String, String], app: String = "editorial-wires")(implicit executionContext: ExecutionContext): Future[Unit] = {

    val telemetryURI = new URI(telemetryUrl)
    val hmacHeaderValues = hmacClient.createHMACHeaderValues(telemetryURI)

    val parameters = List(TelemetryEvent(
      app = app,
      stage = stage.name,
      `type` = eventType,
      value = 1,
      eventTime = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT),
      tags = tags,
    ))
    val body = Json.toJson(parameters)

    wsClient.url(telemetryUrl)
      .withMethod("POST")
      .addHttpHeaders(
        HMACHeaderNames.dateKey -> hmacHeaderValues.date,
        HMACHeaderNames.hmacKey -> hmacHeaderValues.token
      )
      .withBody(body)
      .execute()
      .map { response =>
        if (response.status >= 400) {
          logger.error(s"Failed to send telemetry event, got response ${response.status} - ${response.body}")
        }
        ()
      }
  }

  def sendUserTelemetryEvent(user: User, path: String)(implicit executionContext: ExecutionContext): Future[Unit] = {
    val hostname = if (stage == Prod) "editorial-wires.gutools.co.uk" else "editorial-wires.code.dev-gutools.co.uk"
    val tags: Map[String, String] = Map(
      "email" -> user.email,
      "app" -> "editorial-wires",
      "path" -> path,
      "stage" -> stage.toString(),
      "referrer-hostname" ->  hostname
    )
    sendTelemetryEvent(eventType = "GUARDIAN_TOOL_ACCESSED", tags = tags, app = "user-telemetry")
  }
  
}
