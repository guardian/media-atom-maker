package com.gu.media.iconik

import play.api.libs.json.{JsObject, Json}

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class IconikSyncResult(
    id: String,
    entityType: String,
    action: String,
    updatedChildIds: List[String] = Nil,
    updatedAtomIds: List[String] = Nil
) {
  def toJson: JsObject = Json.obj(
    "id" -> id,
    "entityType" -> entityType,
    "action" -> action,
    "updatedChildIds" -> updatedChildIds,
    "updatedAtomIds" -> updatedAtomIds
  )
}

sealed trait IconikSyncError {
  def message: String
}

case class SyncNotFound(id: String) extends IconikSyncError {
  val message: String = s"Collection $id was not found in Iconik or DynamoDB"
}

case class SyncInvalidHierarchy(id: String, message: String)
    extends IconikSyncError

case class SyncCascadeBlocked(
    id: String,
    entityType: String,
    message: String,
    blockingChildIds: List[String] = Nil,
    blockingAtomIds: List[String] = Nil
) extends IconikSyncError {
  def toJson: JsObject = Json.obj(
    "error" -> message,
    "id" -> id,
    "entityType" -> entityType,
    "blockingChildIds" -> blockingChildIds,
    "blockingAtomIds" -> blockingAtomIds
  )
}

case class SyncDynamoError(failure: DynamoFailure) extends IconikSyncError {
  def message: String = failure.message
}

case class SyncIconikError(failure: IconikRequestFailure)
    extends IconikSyncError {
  def message: String = failure.message
}

class IconikSyncService[E](
    iconikStore: IconikStore[E],
    atomStore: IconikAtomStore,
    iconikApiClient: IconikCollectionClient,
    credentials: IconikCredentials
)(implicit executionContext: ExecutionContext) {

  private val cachedLibraryRoot = new AtomicReference[Option[String]](None)
  private val maxHierarchyDepth = 6

  private sealed trait ResolvedEntity {
    def entityType: String
  }
  private case class ResolvedWorkingGroup(id: String, title: String)
      extends ResolvedEntity {
    val entityType = "working_group"
  }
  private case class ResolvedCommission(
      id: String,
      title: String,
      workingGroupId: String,
      workingGroupTitle: String,
      year: Option[String]
  ) extends ResolvedEntity {
    val entityType = "commission"
  }
  private case class ResolvedProject(
      id: String,
      title: String,
      commissionId: String,
      commissionTitle: String,
      workingGroupId: String,
      workingGroupTitle: String,
      commissionYear: Option[String]
  ) extends ResolvedEntity {
    val entityType = "project"
  }

  private case class StoredState(
      project: Option[IconikProject],
      commission: Option[IconikCommission],
      workingGroup: Option[IconikWorkingGroup]
  ) {
    def storedType: Option[String] =
      project
        .map(_ => "project")
        .orElse(commission.map(_ => "commission"))
        .orElse(workingGroup.map(_ => "working_group"))
  }

  def sync(id: String): Future[Either[IconikSyncError, IconikSyncResult]] = {
    val trimmedId = id.trim
    if (trimmedId.isEmpty) {
      Future.successful(
        Left(SyncInvalidHierarchy(id, "Collection ID must not be empty"))
      )
    } else {
      Future(readStoredState(trimmedId))
        .flatMap {
          case Left(error) => Future.successful(Left(error))
          case Right(stored) =>
            iconikApiClient.getCollection(trimmedId, credentials).flatMap {
              case Left(error) =>
                Future.successful(Left(SyncIconikError(error)))
              case Right(None) =>
                syncMissingFromIconik(trimmedId, stored)
              case Right(Some(collection)) =>
                syncPresentInIconik(trimmedId, collection, stored)
            }
        }
        .recover { case NonFatal(error) =>
          Left(
            SyncDynamoError(
              DynamoFailure(
                s"Unexpected failure syncing Iconik collection $trimmedId",
                Some(error)
              )
            )
          )
        }
    }
  }

  private def readStoredState(
      id: String
  ): Either[IconikSyncError, StoredState] =
    for {
      project <- liftStore(iconikStore.getProject(id))
      commission <- liftStore(iconikStore.getCommission(id))
      workingGroup <- liftStore(iconikStore.getWorkingGroup(id))
    } yield StoredState(project, commission, workingGroup)

  private def syncMissingFromIconik(
      id: String,
      stored: StoredState
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    stored match {
      case StoredState(Some(_), _, _) =>
        withLinkedAtoms { atoms =>
          val blockingAtoms =
            atoms.filter(_.projectId.contains(id)).map(_.atomId).distinct.sorted
          if (blockingAtoms.nonEmpty) {
            Future.successful(
              Left(
                SyncCascadeBlocked(
                  id = id,
                  entityType = "project",
                  message =
                    "Project is missing in Iconik but still referenced by atoms",
                  blockingAtomIds = blockingAtoms
                )
              )
            )
          } else {
            iconikStore.deleteProject(id)
            Future.successful(Right(IconikSyncResult(id, "project", "deleted")))
          }
        }

      case StoredState(None, Some(_), _) =>
        liftStore(iconikStore.getProjectsByCommissionId(id)) match {
          case Left(error) => Future.successful(Left(error))
          case Right(childProjects) if childProjects.nonEmpty =>
            Future.successful(
              Left(
                SyncCascadeBlocked(
                  id = id,
                  entityType = "commission",
                  message =
                    "Commission has child projects; sync child projects first",
                  blockingChildIds = childProjects.map(_.id).sorted
                )
              )
            )
          case Right(_) =>
            withLinkedAtoms { atoms =>
              val blockingAtoms = atoms
                .filter(_.commissionId.contains(id))
                .map(_.atomId)
                .distinct
                .sorted
              if (blockingAtoms.nonEmpty) {
                Future.successful(
                  Left(
                    SyncCascadeBlocked(
                      id = id,
                      entityType = "commission",
                      message =
                        "Commission is missing in Iconik but still referenced by atoms",
                      blockingAtomIds = blockingAtoms
                    )
                  )
                )
              } else {
                iconikStore.deleteCommission(id)
                Future.successful(
                  Right(IconikSyncResult(id, "commission", "deleted"))
                )
              }
            }
        }

      case StoredState(None, None, Some(_)) =>
        liftStore(iconikStore.getCommissionsByWorkingGroupId(id)) match {
          case Left(error) => Future.successful(Left(error))
          case Right(childCommissions) if childCommissions.nonEmpty =>
            Future.successful(
              Left(
                SyncCascadeBlocked(
                  id = id,
                  entityType = "working_group",
                  message =
                    "Working group has child commissions; sync child commissions first",
                  blockingChildIds = childCommissions.map(_.id).sorted
                )
              )
            )
          case Right(_) =>
            withLinkedAtoms { atoms =>
              val blockingAtoms = atoms
                .filter(_.workingGroupId.contains(id))
                .map(_.atomId)
                .distinct
                .sorted
              if (blockingAtoms.nonEmpty) {
                Future.successful(
                  Left(
                    SyncCascadeBlocked(
                      id = id,
                      entityType = "working_group",
                      message =
                        "Working group is missing in Iconik but still referenced by atoms",
                      blockingAtomIds = blockingAtoms
                    )
                  )
                )
              } else {
                iconikStore.deleteWorkingGroup(id)
                Future.successful(
                  Right(IconikSyncResult(id, "working_group", "deleted"))
                )
              }
            }
        }

      case StoredState(None, None, None) =>
        Future.successful(Left(SyncNotFound(id)))
    }

  private def syncPresentInIconik(
      id: String,
      collection: IconikCollection,
      stored: StoredState
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    walkAncestors(id, collection).flatMap {
      case Left(error) => Future.successful(Left(error))
      case Right((rootId, nodesFromTop)) =>
        verifyLibraryRoot(rootId).flatMap {
          case Left(error) => Future.successful(Left(error))
          case Right(()) =>
            classify(id, nodesFromTop, stored) match {
              case Left(error) => Future.successful(Left(error))
              case Right(resolved) =>
                stored.storedType.filter(_ != resolved.entityType) match {
                  case Some(existingType) =>
                    Future.successful(
                      Left(
                        SyncInvalidHierarchy(
                          id,
                          s"Collection $id resolved as ${resolved.entityType} in Iconik, conflicting with stored $existingType"
                        )
                      )
                    )
                  case None =>
                    applyResolved(resolved, stored)
                }
            }
        }
    }

  private def walkAncestors(
      leafId: String,
      leaf: IconikCollection
  ): Future[
    Either[IconikSyncError, (String, Vector[(String, IconikCollection)])]
  ] = {
    val knownRoot = cachedLibraryRoot.get()

    def loop(
        currentId: String,
        current: IconikCollection,
        accBelowRoot: List[(String, IconikCollection)],
        visited: Set[String]
    ): Future[
      Either[IconikSyncError, (String, Vector[(String, IconikCollection)])]
    ] =
      current.parentId match {
        case None =>
          if (accBelowRoot.isEmpty)
            Future.successful(
              Left(
                SyncInvalidHierarchy(
                  leafId,
                  "Cannot sync the top-level root collection directly"
                )
              )
            )
          else
            Future.successful(Right(currentId -> accBelowRoot.toVector))

        case Some(parentId) if knownRoot.contains(parentId) =>
          Future.successful(
            Right(parentId -> ((currentId -> current) :: accBelowRoot).toVector)
          )

        case Some(parentId) if visited.contains(parentId) =>
          Future.successful(
            Left(
              SyncInvalidHierarchy(leafId, "Cycle detected in Iconik hierarchy")
            )
          )

        case Some(_) if accBelowRoot.size >= maxHierarchyDepth =>
          Future.successful(
            Left(
              SyncInvalidHierarchy(
                leafId,
                "Collection exceeds expected Iconik Library hierarchy depth"
              )
            )
          )

        case Some(parentId) =>
          iconikApiClient.getCollection(parentId, credentials).flatMap {
            case Left(error) =>
              Future.successful(Left(SyncIconikError(error)))
            case Right(None) =>
              Future.successful(
                Left(
                  SyncInvalidHierarchy(
                    leafId,
                    s"Ancestor collection $parentId is not visible in Iconik"
                  )
                )
              )
            case Right(Some(parent)) =>
              loop(
                parentId,
                parent,
                (currentId -> current) :: accBelowRoot,
                visited + parentId
              )
          }
      }

    if (knownRoot.contains(leafId))
      Future.successful(
        Left(
          SyncInvalidHierarchy(
            leafId,
            "Cannot sync the top-level root collection directly"
          )
        )
      )
    else
      loop(leafId, leaf, Nil, Set(leafId))
  }

  private def verifyLibraryRoot(
      candidateRootId: String
  ): Future[Either[IconikSyncError, Unit]] =
    cachedLibraryRoot.get() match {
      case Some(expected) if expected == candidateRootId =>
        Future.successful(Right(()))
      case Some(_) =>
        Future.successful(
          Left(
            SyncInvalidHierarchy(
              candidateRootId,
              "Collection is outside the Iconik Library root"
            )
          )
        )
      case None =>
        discoverLibraryRootFromStore().map {
          case Left(error) => Left(error)
          case Right(discoveredRoot) if discoveredRoot == candidateRootId =>
            cachedLibraryRoot.compareAndSet(None, Some(discoveredRoot))
            Right(())
          case Right(_) =>
            Left(
              SyncInvalidHierarchy(
                candidateRootId,
                "Collection is outside the Iconik Library root"
              )
            )
        }
    }

  private def discoverLibraryRootFromStore()
      : Future[Either[IconikSyncError, String]] =
    liftStore(iconikStore.listWorkingGroups()) match {
      case Left(error) => Future.successful(Left(error))
      case Right(groups) =>
        Future
          .traverse(groups)(group =>
            iconikApiClient.getCollection(group.id, credentials)
          )
          .map { results =>
            val visibleParents = results
              .collect { case Right(Some(col)) =>
                col.parentId
              }
              .flatten
              .distinct

            visibleParents match {
              case List(rootId) => Right(rootId)
              case Nil =>
                results
                  .collectFirst { case Left(err) =>
                    Left(SyncIconikError(err))
                  }
                  .getOrElse(
                    Left(
                      SyncInvalidHierarchy(
                        "root",
                        "Could not verify Iconik Library root from stored working groups"
                      )
                    )
                  )
              case multiple =>
                Left(
                  SyncInvalidHierarchy(
                    "root",
                    s"Stored working groups have multiple Iconik parents: ${multiple.mkString(", ")}"
                  )
                )
            }
          }
    }

  private def classify(
      id: String,
      nodesFromTop: Vector[(String, IconikCollection)],
      stored: StoredState
  ): Either[IconikSyncError, ResolvedEntity] = {
    def isFourDigitYear(title: String): Boolean = title.matches("\\d{4}")

    nodesFromTop match {
      case Vector((wgId, wgCol)) =>
        Right(ResolvedWorkingGroup(wgId, wgCol.title))

      case Vector((wgId, wgCol), (commId, commCol)) =>
        Right(
          ResolvedCommission(
            id = commId,
            title = commCol.title,
            workingGroupId = wgId,
            workingGroupTitle = wgCol.title,
            year = None
          )
        )

      case Vector((wgId, wgCol), (midId, midCol), (leafId, leafCol)) =>
        if (isFourDigitYear(midCol.title) && stored.project.isEmpty) {
          Right(
            ResolvedCommission(
              id = leafId,
              title = leafCol.title,
              workingGroupId = wgId,
              workingGroupTitle = wgCol.title,
              year = Some(midCol.title)
            )
          )
        } else {
          Right(
            ResolvedProject(
              id = leafId,
              title = leafCol.title,
              commissionId = midId,
              commissionTitle = midCol.title,
              workingGroupId = wgId,
              workingGroupTitle = wgCol.title,
              commissionYear = None
            )
          )
        }

      case Vector(
            (wgId, wgCol),
            (_, yearCol),
            (commId, commCol),
            (projId, projCol)
          ) if isFourDigitYear(yearCol.title) =>
        Right(
          ResolvedProject(
            id = projId,
            title = projCol.title,
            commissionId = commId,
            commissionTitle = commCol.title,
            workingGroupId = wgId,
            workingGroupTitle = wgCol.title,
            commissionYear = Some(yearCol.title)
          )
        )

      case _ =>
        Left(
          SyncInvalidHierarchy(
            id,
            "Collection does not match Working Group / [Year] / Commission / Project hierarchy"
          )
        )
    }
  }

  private def applyResolved(
      resolved: ResolvedEntity,
      stored: StoredState
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    resolved match {
      case ResolvedWorkingGroup(id, title) =>
        val target = IconikWorkingGroup(id, title)
        val action = stored.workingGroup match {
          case None =>
            iconikStore.upsertWorkingGroup(target)
            "created"
          case Some(existing) if existing == target =>
            "unchanged"
          case Some(_) =>
            iconikStore.upsertWorkingGroup(target)
            "updated"
        }
        Future.successful(
          Right(IconikSyncResult(id, "working_group", action))
        )

      case ResolvedCommission(id, title, wgId, wgTitle, year) =>
        ensureWorkingGroupExists(wgId, wgTitle) match {
          case Left(error) => Future.successful(Left(error))
          case Right(()) =>
            val effectiveYear = year.orElse(stored.commission.flatMap(_.year))
            val target = IconikCommission(wgId, id, title, effectiveYear)
            val reparented =
              stored.commission.exists(_.workingGroupId != wgId)

            if (!reparented) {
              val action = stored.commission match {
                case None =>
                  iconikStore.upsertCommission(target)
                  "created"
                case Some(existing) if existing == target =>
                  "unchanged"
                case Some(_) =>
                  iconikStore.upsertCommission(target)
                  "updated"
              }
              Future.successful(
                Right(IconikSyncResult(id, "commission", action))
              )
            } else {
              syncReparentedCommission(target)
            }
        }

      case ResolvedProject(
            id,
            title,
            commId,
            commTitle,
            wgId,
            wgTitle,
            commYear
          ) =>
        val parentsResult = for {
          _ <- ensureWorkingGroupExists(wgId, wgTitle)
          _ <- ensureCommissionExists(commId, commTitle, wgId, commYear)
        } yield ()

        parentsResult match {
          case Left(error) => Future.successful(Left(error))
          case Right(()) =>
            syncProjectAndAtoms(id, title, commId, wgId, stored.project)
        }
    }

  private def syncReparentedCommission(
      target: IconikCommission
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    liftStore(iconikStore.getProjectsByCommissionId(target.id)) match {
      case Left(error) => Future.successful(Left(error))
      case Right(childProjects) =>
        val staleProjects =
          childProjects.filter(_.workingGroupId != target.workingGroupId)
        val staleProjectIds = staleProjects.map(_.id).toSet

        withLinkedAtoms { atoms =>
          val blockingAtoms = atoms
            .filter { atom =>
              atom.projectId.exists(staleProjectIds.contains) ||
              (atom.commissionId.contains(target.id) &&
                !atom.workingGroupId.contains(target.workingGroupId))
            }
            .map(_.atomId)
            .distinct
            .sorted

          if (blockingAtoms.nonEmpty) {
            Future.successful(
              Left(
                SyncCascadeBlocked(
                  id = target.id,
                  entityType = "commission",
                  message =
                    "Reparenting commission requires updating child projects and atoms; sync child projects first",
                  blockingChildIds = staleProjects.map(_.id).sorted,
                  blockingAtomIds = blockingAtoms
                )
              )
            )
          } else {
            staleProjects.foreach { project =>
              iconikStore.upsertProject(
                project.copy(workingGroupId = target.workingGroupId)
              )
            }
            iconikStore.upsertCommission(target)
            Future.successful(
              Right(
                IconikSyncResult(
                  id = target.id,
                  entityType = "commission",
                  action = "updated",
                  updatedChildIds = staleProjects.map(_.id).sorted
                )
              )
            )
          }
        }
    }

  private def syncProjectAndAtoms(
      projectId: String,
      title: String,
      commissionId: String,
      workingGroupId: String,
      existingProject: Option[IconikProject]
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    withLinkedAtoms { atoms =>
      val staleAtomRefs = atoms.filter { atom =>
        atom.projectId.contains(projectId) &&
        (!atom.commissionId.contains(commissionId) ||
          !atom.workingGroupId.contains(workingGroupId))
      }

      Future
        .traverse(staleAtomRefs) { ref =>
          atomStore
            .updateProjectParents(ref, projectId, commissionId, workingGroupId)
            .map(_.map(updated => if (updated) Some(ref.atomId) else None))
        }
        .map { results =>
          sequence(results).map { maybeIds =>
            val updatedAtomIds = maybeIds.flatten.distinct.sorted
            val target = existingProject match {
              case Some(existing) =>
                existing.copy(
                  title = title,
                  commissionId = commissionId,
                  workingGroupId = workingGroupId
                )
              case None =>
                IconikProject(
                  id = projectId,
                  title = title,
                  status = "CREATED",
                  workingGroupId = workingGroupId,
                  commissionId = commissionId,
                  masterPlaceholderId = None,
                  createdAt = Some(
                    LocalDateTime
                      .now(ZoneOffset.UTC)
                      .format(DateTimeFormatter.ISO_DATE_TIME)
                  )
                )
            }

            val action = existingProject match {
              case None =>
                iconikStore.upsertProject(target)
                "created"
              case Some(existing) if existing == target =>
                if (updatedAtomIds.nonEmpty) "updated" else "unchanged"
              case Some(_) =>
                iconikStore.upsertProject(target)
                "updated"
            }

            IconikSyncResult(
              id = projectId,
              entityType = "project",
              action = action,
              updatedAtomIds = updatedAtomIds
            )
          }
        }
    }

  private def ensureWorkingGroupExists(
      wgId: String,
      wgTitle: String
  ): Either[IconikSyncError, Unit] =
    liftStore(iconikStore.getWorkingGroup(wgId)).map {
      case Some(_) => ()
      case None =>
        iconikStore.upsertWorkingGroup(IconikWorkingGroup(wgId, wgTitle))
    }

  private def ensureCommissionExists(
      commId: String,
      commTitle: String,
      wgId: String,
      year: Option[String]
  ): Either[IconikSyncError, Unit] =
    liftStore(iconikStore.getCommission(commId)).map {
      case Some(_) => ()
      case None =>
        iconikStore.upsertCommission(
          IconikCommission(wgId, commId, commTitle, year)
        )
    }

  private def withLinkedAtoms(
      f: List[AtomIconikRef] => Future[
        Either[IconikSyncError, IconikSyncResult]
      ]
  ): Future[Either[IconikSyncError, IconikSyncResult]] =
    atomStore.listLinkedAtoms().flatMap {
      case Left(error)  => Future.successful(Left(SyncDynamoError(error)))
      case Right(atoms) => f(atoms)
    }

  private def liftStore[A](
      result: Either[E, A]
  ): Either[IconikSyncError, A] =
    result.left.map(err => SyncDynamoError(DynamoFailure(err.toString)))

  private def sequence[A](
      results: List[Either[DynamoFailure, A]]
  ): Either[IconikSyncError, List[A]] =
    results.foldRight(Right(Nil): Either[IconikSyncError, List[A]]) {
      case (Right(value), Right(acc)) => Right(value :: acc)
      case (Left(error), _)           => Left(SyncDynamoError(error))
      case (_, Left(error))           => Left(error)
    }
}
