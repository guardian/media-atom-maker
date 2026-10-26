package com.gu.media.iconik

import org.scanamo.DynamoReadError

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class IconikValidationService(
    iconikStore: IconikStore[DynamoReadError],
    iconikApiClient: IconikCollectionClient,
    credentials: IconikCredentials
)(implicit executionContext: ExecutionContext) {

  private val maxConcurrentRequests = 10

  def validate()
      : Future[Either[IconikValidationError, IconikValidationReport]] = {
    val cached =
      try cachedEntities
      catch {
        case NonFatal(error) =>
          Future.successful(
            Left(
              CachedEntitiesFailure(
                "Failed to read cached Iconik entities",
                Some(error)
              )
            )
          )
      }

    cached.flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right(entities) =>
        validateEntities(entities)
          .map(_.map(IconikValidationReport.fromEntities))
          .recover { case NonFatal(error) =>
            Left(
              IconikRequestFailure(
                "Failed to validate cached entities with Iconik",
                Some(error)
              )
            )
          }
    }
  }

  private def cachedEntities
      : Future[Either[IconikValidationError, List[IconikEntity]]] = {
    val projects = cachedList(iconikStore.listProjectsAsync())(project =>
      IconikEntity(
        "project",
        project.id,
        project.title,
        Some(project.commissionId)
      )
    )
    val commissions =
      cachedList(iconikStore.listCommissionsAsync())(commission =>
        IconikEntity(
          "commission",
          commission.id,
          commission.title,
          Some(commission.workingGroupId)
        )
      )
    val workingGroups =
      cachedList(iconikStore.listWorkingGroupsAsync())(workingGroup =>
        IconikEntity(
          "working_group",
          workingGroup.id,
          workingGroup.title,
          None
        )
      )

    for {
      projectResult <- projects
      commissionResult <- commissions
      workingGroupResult <- workingGroups
    } yield {
      for {
        projects <- projectResult
        commissions <- commissionResult
        workingGroups <- workingGroupResult
      } yield projects ++ commissions ++ workingGroups
    }
  }.recover { case NonFatal(error) =>
    Left(
      CachedEntitiesFailure(
        "Failed to read cached Iconik entities",
        Some(error)
      )
    )
  }

  private def cachedList[A](
      result: Future[Either[DynamoReadError, List[A]]]
  )(
      toEntity: A => IconikEntity
  ): Future[Either[IconikValidationError, List[IconikEntity]]] =
    result.map {
      case Left(error) =>
        Left(CachedEntitiesFailure(error.toString))
      case Right(entities) => Right(entities.map(toEntity))
    }

  private def validateEntities(
      entities: List[IconikEntity]
  ): Future[Either[IconikValidationError, List[IconikEntityValidationResult]]] =
    entities
      .grouped(maxConcurrentRequests)
      .foldLeft(
        Future.successful(
          Right(List.empty[IconikEntityValidationResult]): Either[
            IconikValidationError,
            List[IconikEntityValidationResult]
          ]
        )
      ) { case (validated, batch) =>
        validated.flatMap {
          case Left(error) => Future.successful(Left(error))
          case Right(previousResults) =>
            Future
              .traverse(batch) { entity =>
                iconikApiClient
                  .getCollection(entity.id, credentials)
                  .map(
                    _.map(
                      IconikValidationService.validateEntity(entity, _)
                    )
                  )
              }
              .map { batchResults =>
                sequence(batchResults).map(previousResults ++ _)
              }
        }
      }

  private def sequence[A](
      results: List[Either[IconikValidationError, A]]
  ): Either[IconikValidationError, List[A]] =
    results.foldRight(
      Right(Nil): Either[IconikValidationError, List[A]]
    ) {
      case (Right(result), Right(accumulated)) =>
        Right(result :: accumulated)
      case (Left(error), _) => Left(error)
      case (_, Left(error)) => Left(error)
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
