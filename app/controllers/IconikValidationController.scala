package controllers

import com.gu.media.logging.Logging
import com.gu.pandahmac.HMACAuthActions
import data.DataStores
import play.api.libs.json.{
  JsArray,
  JsBoolean,
  JsNull,
  JsObject,
  JsString,
  JsValue,
  Json
}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import util.AWSConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.Try

private case class CachedIconikEntity(
    entityType: String,
    id: String,
    name: String,
    parentId: Option[String]
)

private case class IconikCredentials(appId: String, token: String)

class IconikValidationController(
    val authActions: HMACAuthActions,
    stores: DataStores,
    awsConfig: AWSConfig,
    wsClient: WSClient,
    val controllerComponents: ControllerComponents
) extends BaseController
    with Logging {

  import authActions.AuthAction

  private implicit val executionContext: ExecutionContext =
    controllerComponents.executionContext

  private val iconikApiUrl = "https://app.iconik.io"
  private val maxConcurrentIconikRequests = 10

  private val iconikCredentials: Either[String, IconikCredentials] =
    awsConfig.getString("aws.secretsmanager.iconikCredentials") match {
      case None =>
        Left(
          "aws.secretsmanager.iconikCredentials must contain the Secrets Manager ARN"
        )
      case Some(secretArn) =>
        awsConfig.getSecret(secretArn).toEither match {
          case Left(error) =>
            log.error(
              s"failed to retrieve Iconik credentials from Secrets Manager secret $secretArn",
              error
            )
            Left("Failed to retrieve Iconik credentials from Secrets Manager")
          case Right(secretValue)
              if secretValue != null && secretValue.nonEmpty =>
            Try(Json.parse(secretValue)).toEither.left
              .map { error =>
                log.error(
                  s"Iconik credentials secret $secretArn is not valid JSON",
                  error
                )
                "Iconik credentials secret is not valid JSON"
              }
              .flatMap {
                case credentials: JsObject =>
                  for {
                    appId <- (credentials \ "app-id")
                      .asOpt[String]
                      .filter(_.nonEmpty)
                      .toRight("Iconik credentials secret is missing app-id")
                    token <- (credentials \ "token")
                      .asOpt[String]
                      .filter(_.nonEmpty)
                      .toRight("Iconik credentials secret is missing token")
                  } yield IconikCredentials(appId, token)
                case _ =>
                  Left("Iconik credentials secret must be a JSON object")
              }
          case Right(_) => Left("Iconik credentials secret is empty")
        }
    }

  private def cachedEntities
      : Future[Either[String, List[CachedIconikEntity]]] = {
    val projects =
      stores.iconikDataStore.listProjectsAsync().map(_.left.map(_.toString))
    val commissions =
      stores.iconikDataStore.listCommissionsAsync().map(_.left.map(_.toString))
    val workingGroups =
      stores.iconikDataStore
        .listWorkingGroupsAsync()
        .map(_.left.map(_.toString))

    for {
      projectResult <- projects
      commissionResult <- commissions
      workingGroupResult <- workingGroups
    } yield {
      for {
        projects <- projectResult
        commissions <- commissionResult
        workingGroups <- workingGroupResult
      } yield {
        projects.map(project =>
          CachedIconikEntity(
            "project",
            project.id,
            project.title,
            Some(project.commissionId)
          )
        ) ++ commissions.map(commission =>
          CachedIconikEntity(
            "commission",
            commission.id,
            commission.title,
            Some(commission.workingGroupId)
          )
        ) ++ workingGroups.map(workingGroup =>
          CachedIconikEntity(
            "working_group",
            workingGroup.id,
            workingGroup.title,
            None
          )
        )
      }
    }
  }

  private def checkEntity(
      entity: CachedIconikEntity,
      appId: String,
      apiToken: String
  ): Future[JsObject] =
    wsClient
      .url(s"$iconikApiUrl/API/assets/v1/collections/${entity.id}/")
      .addHttpHeaders(
        "App-ID" -> appId,
        "Auth-Token" -> apiToken,
        "Accept" -> "application/json"
      )
      .get()
      .map { response =>
        val expectedParentId: JsValue =
          entity.parentId.map(JsString(_)).getOrElse(JsNull)
        response.status match {
          case NOT_FOUND =>
            Json.obj(
              "entityType" -> entity.entityType,
              "id" -> entity.id,
              "exists" -> false,
              "nameMatches" -> JsNull,
              "parentMatches" -> JsNull,
              "expectedName" -> entity.name,
              "iconikName" -> JsNull,
              "expectedParentId" -> expectedParentId,
              "iconikParentId" -> JsNull
            )
          case OK =>
            val iconikCollection = response.json
            val iconikName = (iconikCollection \ "title").as[String]
            val iconikParentId =
              (iconikCollection \ "parent_id").toOption match {
                case None | Some(JsNull)   => None
                case Some(JsString(value)) => Some(value)
                case _ =>
                  throw new IllegalStateException(
                    s"Iconik returned an invalid parent_id for ${entity.id}"
                  )
              }
            val parentMatches: JsValue =
              IconikValidationController.parentMatches(
                entity.parentId,
                iconikParentId
              )
            val iconikParentValue: JsValue =
              iconikParentId.map(JsString(_)).getOrElse(JsNull)

            Json.obj(
              "entityType" -> entity.entityType,
              "id" -> entity.id,
              "exists" -> true,
              "nameMatches" -> (entity.name == iconikName),
              "parentMatches" -> parentMatches,
              "expectedName" -> entity.name,
              "iconikName" -> iconikName,
              "expectedParentId" -> expectedParentId,
              "iconikParentId" -> iconikParentValue
            )
          case status =>
            throw new IllegalStateException(
              s"Iconik returned HTTP $status for ${entity.entityType} ${entity.id}"
            )
        }
      }

  private def checkEntities(
      entities: List[CachedIconikEntity],
      appId: String,
      apiToken: String
  ): Future[List[JsObject]] =
    entities
      .grouped(maxConcurrentIconikRequests)
      .foldLeft(Future.successful(List.empty[JsObject])) {
        case (checkedEntities, batch) =>
          checkedEntities.flatMap { checked =>
            Future
              .traverse(batch)(checkEntity(_, appId, apiToken))
              .map(checked ++ _)
          }
      }

  def validateEntities: Action[AnyContent] = AuthAction.async { _ =>
    iconikCredentials match {
      case Left(error) =>
        Future.successful(ServiceUnavailable(Json.obj("error" -> error)))
      case Right(credentials) =>
        cachedEntities
          .flatMap {
            case Left(error) =>
              log.error(s"failed to list cached iconik entities: $error")
              Future.successful(
                ServiceUnavailable(
                  Json.obj("error" -> "Failed to read cached Iconik entities")
                )
              )
            case Right(entities) =>
              checkEntities(entities, credentials.appId, credentials.token)
                .map { results =>
                  val report = JsArray(results)
                  Ok(
                    Json.obj(
                      "entities" -> report,
                      "summary" -> Json.obj(
                        "total" -> results.size,
                        "missing" -> results.count(result =>
                          (result \ "exists").asOpt[Boolean].contains(false)
                        ),
                        "nameMismatches" -> results.count(result =>
                          (result \ "nameMatches")
                            .asOpt[Boolean]
                            .contains(false)
                        ),
                        "parentMismatches" -> results.count(result =>
                          (result \ "parentMatches")
                            .asOpt[Boolean]
                            .contains(false)
                        )
                      )
                    )
                  )
                }
                .recover { case NonFatal(error) =>
                  log.error(
                    "failed to validate cached Iconik entities",
                    error
                  )
                  BadGateway(
                    Json.obj(
                      "error" -> "Failed to validate entities with Iconik"
                    )
                  )
                }
          }
          .recover { case NonFatal(error) =>
            log.error("failed to read cached Iconik entities", error)
            ServiceUnavailable(
              Json.obj("error" -> "Failed to read cached Iconik entities")
            )
          }
    }
  }
}

private[controllers] object IconikValidationController {
  def parentMatches(
      expectedParentId: Option[String],
      iconikParentId: Option[String]
  ): JsValue =
    expectedParentId match {
      case Some(expected) => JsBoolean(iconikParentId.contains(expected))
      case None           => JsNull
    }
}
