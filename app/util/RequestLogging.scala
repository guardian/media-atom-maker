package util

import javax.inject.{Inject, Provider, Singleton}

import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api._
import play.api.routing.Router

import scala.concurrent.Future

@Singleton
class RequestLogging @Inject() (env: Environment, config: Configuration, sourceMapper: OptionalSourceMapper,
                                router: Provider[Router]) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    super.logServerError(request, exception)
    super.onProdServerError(request, exception)
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.info(s"$statusCode for (${request.method}) [${request.uri}] - $message")
    super.onClientError(request, statusCode, message)
  }
}
