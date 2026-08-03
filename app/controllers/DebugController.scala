package controllers

import play.api.Configuration
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.Future

class DebugController(
    configuration: Configuration,
    val controllerComponents: ControllerComponents
) extends BaseController {

  private val faultInjectionEnabled =
    configuration
      .getOptional[Boolean]("faultInjection.enabled")
      .getOrElse(false)

  def throwSyncException(): Action[AnyContent] = Action {
    if (!faultInjectionEnabled) {
      NotFound
    } else {
      throw new RuntimeException("Intentional sync test exception")
    }
  }

  def throwAsyncException(): Action[AnyContent] = Action.async {
    if (!faultInjectionEnabled) {
      Future.successful(NotFound)
    } else {
      Future.failed(new RuntimeException("Intentional async test exception"))
    }
  }
}
