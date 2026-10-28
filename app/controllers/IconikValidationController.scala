package controllers

import com.gu.media.iconik.{
  DynamoFailure,
  IconikEntityNotFound,
  IconikEntityType,
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

  import authActions.APIHMACAuthAction

  private implicit val executionContext: ExecutionContext =
    controllerComponents.executionContext

  def listEntities: Action[AnyContent] = APIHMACAuthAction.async { _ =>
    validationService.listEntityIds().map {
      case Right(entityIds) =>
        Ok(
          Json.toJson(
            entityIds.map(entity =>
              Json.obj("entityType" -> entity.entityType, "id" -> entity.id)
            )
          )
        )
      case Left(error) =>
        logFailure("failed to read Iconik entity IDs from Dynamo", error)
        ServiceUnavailable(
          Json.obj("error" -> "Failed to read Iconik entity IDs from Dynamo")
        )
    }
  }

  def validateEntity(entityType: String, id: String): Action[AnyContent] =
    APIHMACAuthAction.async { _ =>
      IconikEntityType.fromString(entityType) match {
        case None =>
          scala.concurrent.Future.successful(
            BadRequest(
              Json.obj("error" -> s"Unknown Iconik entity type: $entityType")
            )
          )
        case Some(validEntityType) =>
          validationService.validate(validEntityType, id).map {
            case Right(report) => Ok(report.toJson)
            case Left(error: DynamoFailure) =>
              logFailure("failed to read Iconik entity from Dynamo", error)
              ServiceUnavailable(
                Json.obj("error" -> "Failed to read Iconik entity from Dynamo")
              )
            case Left(error: IconikRequestFailure) =>
              logFailure("failed to validate Iconik entity", error)
              BadGateway(
                Json.obj("error" -> "Failed to validate entity with Iconik")
              )
            case Left(error: IconikEntityNotFound) =>
              NotFound(Json.obj("error" -> error.message))
          }
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
