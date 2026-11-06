package com.gu.media.iconik

import org.scanamo.DynamoReadError
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class IconikValidationService(
    iconikStore: IconikStore[DynamoReadError],
    iconikApiClient: IconikCollectionClient,
    credentials: IconikCredentials
)(implicit executionContext: ExecutionContext) {

  def listEntityIds(): Future[Either[DynamoFailure, List[IconikEntityId]]] =
    try {
      val projects =
        normalisedEntityIds(iconikStore.listProjectsAsync())(project =>
          IconikEntityId(IconikEntityType.Project.value, project.id)
        )
      val commissions =
        normalisedEntityIds(iconikStore.listCommissionsAsync())(commission =>
          IconikEntityId(IconikEntityType.Commission.value, commission.id)
        )
      val workingGroups =
        normalisedEntityIds(iconikStore.listWorkingGroupsAsync())(
          workingGroup =>
            IconikEntityId(
              IconikEntityType.WorkingGroup.value,
              workingGroup.id
            )
        )

      for {
        projectIds <- projects
        commissionIds <- commissions
        workingGroupIds <- workingGroups
      } yield {
        for {
          projects <- projectIds
          commissions <- commissionIds
          workingGroups <- workingGroupIds
        } yield projects ++ commissions ++ workingGroups
      }
    } catch {
      case NonFatal(error) =>
        Future.successful(
          Left(
            DynamoFailure(
              "Failed to read Iconik entity IDs from Dynamo",
              Some(error)
            )
          )
        )
    }

  def validate(
      entityType: IconikEntityType,
      id: String
  ): Future[Either[IconikValidationError, IconikValidationReport]] = {
    validateInternal(entityType, id, createPlan = false).map(_.map(_._1))
  }

  def validateWithPlan(
      entityType: IconikEntityType,
      id: String
  ): Future[
    Either[
      IconikValidationError,
      (IconikValidationReport, Option[IconikRepairDecision])
    ]
  ] =
    validateInternal(entityType, id, createPlan = true)

  private def validateInternal(
      entityType: IconikEntityType,
      id: String,
      createPlan: Boolean
  ): Future[
    Either[
      IconikValidationError,
      (IconikValidationReport, Option[IconikRepairDecision])
    ]
  ] = {
    val entityResult =
      try getEntity(entityType, id)
      catch {
        case NonFatal(error) =>
          Left(
            DynamoFailure(
              s"Failed to read Iconik ${entityType.value} from Dynamo",
              Some(error)
            )
          )
      }

    entityResult match {
      case Left(error) => Future.successful(Left(error))
      case Right(None) =>
        Future.successful(Left(IconikEntityNotFound(entityType.value, id)))
      case Right(Some(storedEntity)) =>
        iconikApiClient
          .getCollection(storedEntity.entity.id, credentials)
          .map { result =>
            result.flatMap { collection =>
              val validationResult =
                IconikValidationService.validateEntity(
                  storedEntity.entity,
                  collection
                )
              val repairDecision: Either[
                DynamoFailure,
                Option[IconikRepairDecision]
              ] =
                if (createPlan) planRepair(storedEntity, collection)
                else Right(None)
              repairDecision match {
                case Left(error) => Left(error: IconikValidationError)
                case Right(decision) =>
                  Right(
                    (
                      IconikValidationReport.fromEntities(
                        List(validationResult)
                      ),
                      decision
                    )
                  )
              }
            }
          }
          .recover { case NonFatal(error) =>
            Left(
              IconikRequestFailure(
                s"Failed to validate ${entityType.value} $id with Iconik",
                Some(error)
              )
            ): Either[
              IconikValidationError,
              (IconikValidationReport, Option[IconikRepairDecision])
            ]
          }
    }
  }

  private def planRepair(
      storedEntity: StoredIconikEntity,
      collection: Option[IconikCollection]
  ): Either[DynamoFailure, Option[IconikRepairDecision]] =
    try {
      def itemJson(entity: StoredIconikEntity): JsObject =
        entity match {
          case StoredProject(project) =>
            Json.toJson(project)(IconikProject.format).as[JsObject]
          case StoredCommission(commission) =>
            Json.toJson(commission)(IconikCommission.format).as[JsObject]
          case StoredWorkingGroup(workingGroup) =>
            Json.toJson(workingGroup)(IconikWorkingGroup.format).as[JsObject]
        }

      def updatedOrUnrepairable
          : Either[IconikUnrepairable, StoredIconikEntity] =
        storedEntity match {
          case StoredProject(project) =>
            collection match {
              case None =>
                Right(
                  StoredProject(
                    project.copy(
                      title =
                        IconikValidationService.markDeletedTitle(project.title)
                    )
                  )
                )
              case Some(col) =>
                col.parentId match {
                  case None if col.title == project.title =>
                    Right(StoredProject(project))
                  case None =>
                    Right(StoredProject(project.copy(title = col.title)))
                  case Some(updatedCommissionId)
                      if updatedCommissionId == project.commissionId =>
                    Right(StoredProject(project.copy(title = col.title)))
                  case Some(updatedCommissionId) =>
                    iconikStore.getCommission(updatedCommissionId) match {
                      case Right(Some(commission)) =>
                        Right(
                          StoredProject(
                            project.copy(
                              title = col.title,
                              commissionId = updatedCommissionId,
                              workingGroupId = commission.workingGroupId
                            )
                          )
                        )
                      case Right(None) =>
                        Left(
                          IconikUnrepairable(
                            storedEntity.entity.entityType,
                            storedEntity.entity.id,
                            s"Iconik commission $updatedCommissionId is missing from Dynamo"
                          )
                        )
                      case Left(error) =>
                        throw new IllegalStateException(
                          s"Failed to read Iconik commission $updatedCommissionId from Dynamo: $error"
                        )
                    }
                }
            }

          case StoredCommission(commission) =>
            Right(
              StoredCommission(
                collection match {
                  case None =>
                    commission.copy(
                      title = IconikValidationService.markDeletedTitle(
                        commission.title
                      )
                    )
                  case Some(col) =>
                    commission.copy(
                      title = col.title,
                      workingGroupId =
                        col.parentId.getOrElse(commission.workingGroupId)
                    )
                }
              )
            )

          case StoredWorkingGroup(workingGroup) =>
            Right(
              StoredWorkingGroup(
                collection match {
                  case None =>
                    workingGroup.copy(
                      title = IconikValidationService.markDeletedTitle(
                        workingGroup.title
                      )
                    )
                  case Some(col) =>
                    workingGroup.copy(title = col.title)
                }
              )
            )
        }

      updatedOrUnrepairable match {
        case Left(unrepairable) => Right(Some(unrepairable))
        case Right(updatedEntity) if updatedEntity == storedEntity =>
          Right(None)
        case Right(updatedEntity) =>
          Right(
            Some(
              IconikRepairPlan(
                storedEntity.entity.entityType,
                storedEntity.entity.id,
                itemJson(storedEntity),
                itemJson(updatedEntity)
              )
            )
          )
      }
    } catch {
      case NonFatal(error) =>
        Left(
          DynamoFailure(
            s"Failed to create a repair plan for Iconik ${storedEntity.entity.entityType} ${storedEntity.entity.id}",
            Some(error)
          )
        )
    }

  private def normalisedEntityIds[A](
      result: Future[Either[DynamoReadError, List[A]]]
  )(
      toEntityId: A => IconikEntityId
  ): Future[Either[DynamoFailure, List[IconikEntityId]]] =
    result
      .map {
        case Left(error)  => Left(DynamoFailure(error.toString))
        case Right(items) => Right(items.map(toEntityId))
      }
      .recover { case NonFatal(error) =>
        Left(
          DynamoFailure(
            "Failed to read Iconik entity IDs from Dynamo",
            Some(error)
          )
        )
      }

  private sealed trait StoredIconikEntity {
    def entity: IconikEntity
  }
  private case class StoredProject(project: IconikProject)
      extends StoredIconikEntity {
    override val entity: IconikEntity = IconikEntity(
      IconikEntityType.Project.value,
      project.id,
      project.title,
      Some(project.commissionId)
    )
  }
  private case class StoredCommission(commission: IconikCommission)
      extends StoredIconikEntity {
    override val entity: IconikEntity = IconikEntity(
      IconikEntityType.Commission.value,
      commission.id,
      commission.title,
      Some(commission.workingGroupId)
    )
  }
  private case class StoredWorkingGroup(workingGroup: IconikWorkingGroup)
      extends StoredIconikEntity {
    override val entity: IconikEntity = IconikEntity(
      IconikEntityType.WorkingGroup.value,
      workingGroup.id,
      workingGroup.title,
      None
    )
  }

  private def getEntity(
      entityType: IconikEntityType,
      id: String
  ): Either[IconikValidationError, Option[StoredIconikEntity]] =
    entityType match {
      case IconikEntityType.Project =>
        iconikStore
          .getProject(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(_.map(StoredProject))
      case IconikEntityType.Commission =>
        iconikStore
          .getCommission(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(_.map(StoredCommission))
      case IconikEntityType.WorkingGroup =>
        iconikStore
          .getWorkingGroup(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(_.map(StoredWorkingGroup))
    }

}

object IconikValidationService {
  private val DeletedPrefix = "[deleted]"

  def markDeletedTitle(title: String): String =
    if (title.trim.toLowerCase.startsWith(DeletedPrefix)) title
    else s"$DeletedPrefix $title"

  def validateEntity(
      entity: IconikEntity,
      collection: Option[IconikCollection]
  ): IconikEntityValidationResult =
    collection match {
      case None =>
        IconikEntityValidationResult(
          entityType = entity.entityType,
          id = entity.id,
          exists = false,
          nameMatches = None,
          parentMatches = None,
          expectedName = entity.name,
          iconikName = None,
          expectedParentId = entity.parentId,
          iconikParentId = None
        )
      case Some(collection) =>
        IconikEntityValidationResult(
          entityType = entity.entityType,
          id = entity.id,
          exists = true,
          nameMatches = Some(entity.name == collection.title),
          parentMatches = entity.parentId.map(collection.parentId.contains),
          expectedName = entity.name,
          iconikName = Some(collection.title),
          expectedParentId = entity.parentId,
          iconikParentId = collection.parentId
        )
    }
}
