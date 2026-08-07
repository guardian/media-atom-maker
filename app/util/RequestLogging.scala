package util

import com.gu.media.logging.Logging
import io.sentry.Sentry
import java.net.URI

import javax.inject.{Inject, Provider, Singleton}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.{Logging => _, _}
import play.api.routing.Router

import scala.concurrent.Future

//noinspection ScalaUnusedSymbol
@Singleton
class RequestLogging @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with Logging {

  private val stage = config.getOptional[String]("stage").getOrElse("DEV")
  private val sentryDsn = config.getOptional[String]("raven.url").getOrElse("")
  private val sentryLocalEnabled =
    config.getOptional[Boolean]("sentry.local.enabled").getOrElse(false)
  private val sentryEnabled =
    sentryDsn.nonEmpty && (stage != "DEV" || sentryLocalEnabled)

  private val sentryTarget = {
    try {
      val uri = URI.create(sentryDsn)
      val host = Option(uri.getHost).getOrElse("unknown-host")
      val projectId =
        Option(uri.getPath)
          .getOrElse("")
          .split('/')
          .filter(_.nonEmpty)
          .lastOption
          .getOrElse("unknown-project")
      s"$host/$projectId"
    } catch {
      case _: Throwable => "unparseable-dsn"
    }
  }

  private val sentryDisabledReason = {
    if (sentryDsn.isEmpty) {
      "raven.url is not configured"
    } else if (stage == "DEV" && !sentryLocalEnabled) {
      "stage is DEV and sentry.local.enabled is false"
    } else {
      "unknown"
    }
  }

  if (sentryEnabled) {
    Sentry.init(options => {
      options.setDsn(sentryDsn)
      options.setEnvironment(stage)
      options.setAttachStacktrace(true)
    })
    log.info(
      s"Sentry enabled for stage=$stage targeting=$sentryTarget"
    )
  } else {
    log.warn(s"Sentry disabled: $sentryDisabledReason")
  }

  private def captureInSentry(
      request: RequestHeader,
      exception: Throwable
  ): Unit = {
    if (sentryEnabled) {
      Sentry.withScope(scope => {
        scope.setTag("http.method", request.method)
        scope.setTag("http.path", request.path)
        scope.setTag("http.host", request.host)
        scope.setTag("http.status_code", "500")
        scope.setTag("stage", stage)
        scope.setExtra("request.uri", request.uri)
        scope.setExtra("request.queryString", request.rawQueryString)
        scope.setExtra("request.remoteAddress", request.remoteAddress)
        scope.setExtra(
          "request.userAgent",
          request.headers.get("User-Agent").getOrElse("")
        )
        scope.setExtra(
          "request.id",
          request.headers
            .get("X-Request-Id")
            .orElse(request.headers.get("x-request-id"))
            .getOrElse("")
        )
        val eventId = Sentry.captureException(exception)
        // Flush so local one-off test exceptions are sent before request teardown.
        val flushed = Sentry.flush(2000)
        log.info(
          s"Sentry capture attempted for ${request.method} ${request.uri}, eventId=$eventId, flushed=$flushed"
        )
      })
    }
  }

  private def captureAndLogServerError(
      request: RequestHeader,
      exception: UsefulException
  ): Unit = {
    captureInSentry(request, exception)
    super.logServerError(request, exception)
  }

  override def onServerError(
      request: RequestHeader,
      exception: Throwable
  ): Future[Result] = {
    captureInSentry(request, exception)
    log.error(
      s"Server error for (${request.method}) [${request.uri}]",
      exception
    )
    super.onServerError(request, exception)
  }

  override def onProdServerError(
      request: RequestHeader,
      exception: UsefulException
  ): Future[Result] = {
    captureAndLogServerError(request, exception)
    super.onProdServerError(request, exception)
  }

  override def onClientError(
      request: RequestHeader,
      statusCode: Int,
      message: String
  ): Future[Result] = {
    log.info(s"$statusCode for (${request.method}) [${request.uri}] - $message")
    super.onClientError(request, statusCode, message)
  }
}
