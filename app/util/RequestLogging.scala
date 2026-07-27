package util

import com.gu.media.logging.Logging
import io.sentry.Sentry

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
  private val sentryEnabled =
    sentryDsn.nonEmpty && stage != "DEV"

  if (sentryEnabled) {
    Sentry.init(options => {
      options.setDsn(sentryDsn)
      options.setEnvironment(stage)
      options.setAttachStacktrace(true)
    })
  }

  override def onProdServerError(
      request: RequestHeader,
      exception: UsefulException
  ): Future[Result] = {
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
        Sentry.captureException(exception)
      })
    }
    super.logServerError(request, exception)
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
