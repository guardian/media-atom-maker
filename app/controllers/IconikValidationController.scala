package controllers

import com.gu.media.iconik.{
  DynamoFailure,
  IconikRequestFailure,
  IconikValidationError,
  IconikValidationService
}
import com.gu.media.logging.Logging
import com.gu.pandahmac.HMACAuthActions
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import scala.concurrent.ExecutionContext

class IconikValidationController(
    val authActions: HMACAuthActions,
    validationService: IconikValidationService,
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {

  import authActions.AuthAction

  private implicit val executionContext: ExecutionContext =
    controllerComponents.executionContext

  def validateEntities: Action[AnyContent] = AuthAction.async { _ =>
    validationService.validate().map {
      case Right(report) => Ok(report.toJson)
      case Left(error: DynamoFailure) =>
        logFailure("failed to read Iconik entities from Dynamo", error)
        ServiceUnavailable(
          Json.obj("error" -> "Failed to read Iconik entities from Dynamo")
        )
      case Left(error: IconikRequestFailure) =>
        logFailure("failed to validate Iconik entities", error)
        BadGateway(
          Json.obj("error" -> "Failed to validate entities with Iconik")
        )
    }
  }

  private def logFailure(
      message: String,
      error: IconikValidationError
  ): Unit =
    error.cause match {
      case Some(cause) => log.error(message, cause)
      case None        => log.error(s"$message: ${error.message}")
    }
}
