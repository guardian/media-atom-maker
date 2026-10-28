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
      id: String
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
      case Right(Some(entity)) =>
        iconikApiClient
          .getCollection(entity.id, credentials)
          .map(
            _.map(collection =>
              IconikValidationReport.fromEntities(
                List(IconikValidationService.validateEntity(entity, collection))
              )
            )
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

  private def getEntity(
      entityType: IconikEntityType,
      id: String
  ): Either[IconikValidationError, Option[IconikEntity]] =
    entityType match {
      case IconikEntityType.Project =>
        iconikStore
          .getProject(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(
            _.map(project =>
              IconikEntity(
                entityType.value,
                project.id,
                project.title,
                Some(project.commissionId)
              )
            )
          )
      case IconikEntityType.Commission =>
        iconikStore
          .getCommission(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(
            _.map(commission =>
              IconikEntity(
                entityType.value,
                commission.id,
                commission.title,
                Some(commission.workingGroupId)
              )
            )
          )
      case IconikEntityType.WorkingGroup =>
        iconikStore
          .getWorkingGroup(id)
          .left
          .map(error => DynamoFailure(error.toString))
          .map(
            _.map(workingGroup =>
              IconikEntity(
                entityType.value,
                workingGroup.id,
                workingGroup.title,
                None
              )
            )
          )
    }
}

object IconikValidationService {
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
