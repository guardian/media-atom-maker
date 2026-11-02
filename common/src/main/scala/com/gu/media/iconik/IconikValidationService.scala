package com.gu.media.iconik

import org.scanamo.DynamoReadError

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
      id: String,
      repair: Boolean = false
  ): Future[Either[IconikValidationError, IconikValidationReport]] = {
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
          .map(
            _.flatMap { collection =>
              val validationResult =
                IconikValidationService.validateEntity(
                  storedEntity.entity,
                  collection
                )
              if (repair) {
                repairEntity(storedEntity, collection).map(wasRepaired =>
                  IconikValidationReport.fromEntities(
                    List(validationResult.copy(repaired = wasRepaired))
                  )
                )
              } else {
                Right(
                  IconikValidationReport.fromEntities(List(validationResult))
                )
              }
            }
          )
          .recover { case NonFatal(error) =>
            Left(
              IconikRequestFailure(
                s"Failed to validate ${entityType.value} $id with Iconik",
                Some(error)
              )
            )
          }
    }
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

  private def repairEntity(
      storedEntity: StoredIconikEntity,
      collection: Option[IconikCollection]
  ): Either[DynamoFailure, Boolean] =
    try {
      storedEntity match {
        case StoredProject(project) =>
          val updated = collection match {
            case None =>
              project.copy(
                title = IconikValidationService.markDeletedTitle(project.title)
              )
            case Some(col) =>
              val updatedCommissionId =
                col.parentId.getOrElse(project.commissionId)
              val updatedWorkingGroupId =
                if (updatedCommissionId == project.commissionId)
                  project.workingGroupId
                else
                  iconikStore
                    .getCommission(updatedCommissionId)
                    .toOption
                    .flatten
                    .map(_.workingGroupId)
                    .getOrElse(project.workingGroupId)
              project.copy(
                title = col.title,
                commissionId = updatedCommissionId,
                workingGroupId = updatedWorkingGroupId
              )
          }
          if (updated != project) {
            iconikStore.upsertProject(updated)
            Right(true)
          } else Right(false)

        case StoredCommission(commission) =>
          val updated = collection match {
            case None =>
              commission.copy(
                title =
                  IconikValidationService.markDeletedTitle(commission.title)
              )
            case Some(col) =>
              commission.copy(
                title = col.title,
                workingGroupId =
                  col.parentId.getOrElse(commission.workingGroupId)
              )
          }
          if (updated != commission) {
            iconikStore.upsertCommission(updated)
            Right(true)
          } else Right(false)

        case StoredWorkingGroup(workingGroup) =>
          val updated = collection match {
            case None =>
              workingGroup.copy(
                title =
                  IconikValidationService.markDeletedTitle(workingGroup.title)
              )
            case Some(col) =>
              workingGroup.copy(title = col.title)
          }
          if (updated != workingGroup) {
            iconikStore.upsertWorkingGroup(updated)
            Right(true)
          } else Right(false)
      }
    } catch {
      case NonFatal(error) =>
        Left(
          DynamoFailure(
            s"Failed to repair Iconik ${storedEntity.entity.entityType} ${storedEntity.entity.id} in Dynamo",
            Some(error)
          )
        )
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
