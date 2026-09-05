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
    router: Provider[Router],
    sentry: SentryConfig
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
    with Logging {

  private def captureInSentry(
      request: RequestHeader,
      exception: Throwable
  ): Unit = {

    Sentry.withScope(scope => {
      scope.setTag("http.method", request.method)
      scope.setTag("http.host", request.host)
      // We dont want query string params in the tags so we use the route pattern
      request.attrs
        .get(Router.Attrs.HandlerDef)
        .foreach(handler => scope.setTag("http.route", handler.path))
      scope.setExtra("request.uri", request.uri)
      scope.setExtra("request.queryString", request.rawQueryString)
      scope.setExtra("request.remoteAddress", request.remoteAddress)
      scope.setExtra(
        "request.userAgent",
        request.headers.get("User-Agent").getOrElse("")
      )
      scope.setExtra(
        "request.id",
        request.headers.get("X-Request-Id").getOrElse("")
      )
      Sentry.captureException(exception)
    })
  }

  override protected def logServerError(
      request: RequestHeader,
      usefulException: UsefulException
  ): Unit = {
    if (sentry.enabled) {
      captureInSentry(request, usefulException)
    }
    super.logServerError(request, usefulException)
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
