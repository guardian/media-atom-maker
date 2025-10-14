package util

import com.gu.media.logging.Logging

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

  override def onProdServerError(
      request: RequestHeader,
      exception: UsefulException
  ): Future[Result] = {
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
