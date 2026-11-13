package com.gu.media.iconik

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsNull, JsObject, Json, OWrites, Writes}

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class IconikSyncChange(
    id: String,
    entityType: String,
    action: String,
    fields: JsObject,
    isPublished: Option[Boolean] = None
)

object IconikSyncChange {
  implicit val writes: OWrites[IconikSyncChange] = Json.writes[IconikSyncChange]
}

case class IconikSyncResult(
    id: String,
    entityType: String,
    action: String,
    updatedChildIds: List[String] = Nil,
    updatedAtomIds: List[String] = Nil,
    dryRun: Boolean = false,
    changes: List[IconikSyncChange] = Nil
) {
  def toJson: JsObject = Json.obj(
    "id" -> id,
    "entityType" -> entityType,
    "action" -> action,
    "updatedChildIds" -> updatedChildIds,
    "updatedAtomIds" -> updatedAtomIds,
    "dryRun" -> dryRun,
    "changes" -> changes
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

case class SyncWriteConflict(id: String, entityType: String)
    extends IconikSyncError {
  val message = s"$entityType $id changed during sync; preview and retry"
}

/** Sync cannot proceed while these child collections or atom references remain
  * unresolved.
  */
case class SyncCascadeBlocked(
    id: String,
    entityType: String,
    message: String,
    blockingChildIds: List[String] = Nil,
    atomIdsPreventingSync: List[String] = Nil
) extends IconikSyncError {
  def toJson: JsObject = Json.obj(
    "error" -> message,
    "id" -> id,
    "entityType" -> entityType,
    "blockingChildIds" -> blockingChildIds,
    "atomIdsPreventingSync" -> atomIdsPreventingSync
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

  private type SyncResult[A] = EitherT[Future, IconikSyncError, A]

  private case class PendingChange(
      change: IconikSyncChange,
      run: () => Future[Either[IconikSyncError, Option[IconikSyncChange]]]
  )

  /** Keeps preview read-only and orders writes to preserve hierarchy
    * references.
    */
  private case class SyncPlan(
      id: String,
      entityType: String,
      steps: List[List[PendingChange]]
  ) {
    def changes: List[IconikSyncChange] = steps.flatten.map(_.change)

    def result(
        changes: List[IconikSyncChange],
        dryRun: Boolean
    ): IconikSyncResult =
      IconikSyncResult(
        id = id,
        entityType = entityType,
        action = changes
          .find(c => c.id == id && c.entityType == entityType)
          .map(_.action)
          .getOrElse(
            if (changes.exists(_.entityType == "atom")) "updated"
            else "unchanged"
          ),
        updatedChildIds = changes
          .filter(c => c.entityType == "project" && c.id != id)
          .map(_.id)
          .distinct
          .sorted,
        updatedAtomIds =
          changes.filter(_.entityType == "atom").map(_.id).distinct.sorted,
        dryRun = dryRun,
        changes = changes
      )
  }

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

  def sync(
      id: String,
      dryRun: Boolean = false
  ): Future[Either[IconikSyncError, IconikSyncResult]] = {
    val trimmedId = id.trim
    if (trimmedId.isEmpty) {
      Future.successful(
        Left(SyncInvalidHierarchy(id, "Collection ID must not be empty"))
      )
    } else {
      val result = for {
        stored <- EitherT(Future(readStoredState(trimmedId)))
        collection <- fetchCollection(trimmedId)
        plan <- collection match {
          case None        => planMissingFromIconik(trimmedId, stored)
          case Some(value) => planPresentInIconik(trimmedId, value, stored)
        }
        changes <-
          if (dryRun) {
            EitherT.rightT[Future, IconikSyncError](plan.changes)
          } else {
            plan.steps.foldM(List.empty[IconikSyncChange]) { (applied, step) =>
              EitherT(Future.traverse(step)(_.run()).map(_.sequence))
                .map(results => applied ++ results.flatten)
            }
          }
      } yield plan.result(changes, dryRun)

      result.value
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

  private def planMissingFromIconik(
      id: String,
      stored: StoredState
  ): SyncResult[SyncPlan] =
    stored match {
      case StoredState(Some(project), _, _) =>
        for {
          atoms <- linkedAtoms
          atomIdsPreventingSync =
            atoms.filter(_.projectId.contains(id)).map(_.atomId).distinct.sorted
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            atomIdsPreventingSync.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "project",
              message =
                "Project is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          "project",
          planCollectionChange("project", id, Some(project), None) {
            Right(iconikStore.deleteProject(id))
          }
        )

      case StoredState(None, Some(commission), _) =>
        for {
          childProjects <- EitherT.fromEither[Future](
            liftStore(iconikStore.getProjectsByCommissionId(id))
          )
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            childProjects.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "commission",
              message =
                "Commission has child projects; sync child projects first",
              blockingChildIds = childProjects.map(_.id).sorted
            )
          )
          atoms <- linkedAtoms
          atomIdsPreventingSync = atoms
            .filter(_.commissionId.contains(id))
            .map(_.atomId)
            .distinct
            .sorted
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            atomIdsPreventingSync.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "commission",
              message =
                "Commission is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          "commission",
          planCollectionChange("commission", id, Some(commission), None) {
            Right(iconikStore.deleteCommission(id))
          }
        )

      case StoredState(None, None, Some(workingGroup)) =>
        for {
          childCommissions <- EitherT.fromEither[Future](
            liftStore(iconikStore.getCommissionsByWorkingGroupId(id))
          )
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            childCommissions.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "working_group",
              message =
                "Working group has child commissions; sync child commissions first",
              blockingChildIds = childCommissions.map(_.id).sorted
            )
          )
          projects <- EitherT.fromEither[Future](
            liftStore(iconikStore.listProjects())
          )
          referencingProjectIds = projects
            .filter(_.workingGroupId == id)
            .map(_.id)
            .sorted
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            referencingProjectIds.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "working_group",
              message =
                "Working group is still referenced by projects; sync those projects first",
              blockingChildIds = referencingProjectIds
            )
          )
          atoms <- linkedAtoms
          atomIdsPreventingSync = atoms
            .filter(_.workingGroupId.contains(id))
            .map(_.atomId)
            .distinct
            .sorted
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            atomIdsPreventingSync.isEmpty,
            (),
            SyncCascadeBlocked(
              id = id,
              entityType = "working_group",
              message =
                "Working group is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          "working_group",
          planCollectionChange("working_group", id, Some(workingGroup), None) {
            Right(iconikStore.deleteWorkingGroup(id))
          }
        )

      case StoredState(None, None, None) =>
        EitherT.leftT[Future, SyncPlan](SyncNotFound(id))
    }

  private def planPresentInIconik(
      id: String,
      collection: IconikCollection,
      stored: StoredState
  ): SyncResult[SyncPlan] =
    for {
      ancestors <- walkAncestors(id, collection)
      (rootId, nodesFromTop) = ancestors
      _ <- verifyLibraryRoot(rootId)
      resolved <- EitherT.fromEither[Future](classify(id, nodesFromTop, stored))
      _ <- EitherT.fromEither[Future](
        stored.storedType.filter(_ != resolved.entityType).toLeft(()).left.map {
          existingType =>
            SyncInvalidHierarchy(
              id,
              s"Collection $id resolved as ${resolved.entityType} in Iconik, conflicting with stored $existingType"
            )
        }
      )
      result <- planResolved(resolved, stored)
    } yield result

  private def walkAncestors(
      leafId: String,
      leaf: IconikCollection
  ): SyncResult[(String, Vector[(String, IconikCollection)])] = {
    val knownRoot = cachedLibraryRoot.get()

    def loop(
        currentId: String,
        current: IconikCollection,
        accBelowRoot: List[(String, IconikCollection)],
        visited: Set[String]
    ): SyncResult[(String, Vector[(String, IconikCollection)])] =
      current.parentId match {
        case None =>
          EitherT.cond[Future](
            accBelowRoot.nonEmpty,
            currentId -> accBelowRoot.toVector,
            SyncInvalidHierarchy(
              leafId,
              "Cannot sync the top-level root collection directly"
            )
          )

        case Some(parentId) if knownRoot.contains(parentId) =>
          EitherT.rightT[Future, IconikSyncError](
            parentId -> ((currentId -> current) :: accBelowRoot).toVector
          )

        case Some(parentId) if visited.contains(parentId) =>
          EitherT.leftT[Future, (String, Vector[(String, IconikCollection)])](
            SyncInvalidHierarchy(leafId, "Cycle detected in Iconik hierarchy")
          )

        case Some(_) if accBelowRoot.size >= maxHierarchyDepth =>
          EitherT.leftT[Future, (String, Vector[(String, IconikCollection)])](
            SyncInvalidHierarchy(
              leafId,
              "Collection exceeds expected Iconik Library hierarchy depth"
            )
          )

        case Some(parentId) =>
          for {
            parent <- fetchCollection(parentId).subflatMap(
              _.toRight(
                SyncInvalidHierarchy(
                  leafId,
                  s"Ancestor collection $parentId is not visible in Iconik"
                )
              )
            )
            result <- loop(
              parentId,
              parent,
              (currentId -> current) :: accBelowRoot,
              visited + parentId
            )
          } yield result
      }

    if (knownRoot.contains(leafId))
      EitherT.leftT[Future, (String, Vector[(String, IconikCollection)])](
        SyncInvalidHierarchy(
          leafId,
          "Cannot sync the top-level root collection directly"
        )
      )
    else
      loop(leafId, leaf, Nil, Set(leafId))
  }

  private def verifyLibraryRoot(
      candidateRootId: String
  ): SyncResult[Unit] =
    cachedLibraryRoot.get() match {
      case Some(expected) if expected == candidateRootId =>
        EitherT.rightT[Future, IconikSyncError](())
      case Some(_) =>
        EitherT.leftT[Future, Unit](
          SyncInvalidHierarchy(
            candidateRootId,
            "Collection is outside the Iconik Library root"
          )
        )
      case None =>
        for {
          discoveredRoot <- discoverLibraryRootFromStore()
          _ <- EitherT.cond[Future][IconikSyncError, Unit](
            discoveredRoot == candidateRootId,
            (),
            SyncInvalidHierarchy(
              candidateRootId,
              "Collection is outside the Iconik Library root"
            )
          )
        } yield {
          cachedLibraryRoot.compareAndSet(None, Some(discoveredRoot))
          ()
        }
    }

  private def discoverLibraryRootFromStore(): SyncResult[String] =
    for {
      groups <- EitherT.fromEither[Future](
        liftStore(iconikStore.listWorkingGroups())
      )
      rootId <- EitherT[Future, IconikSyncError, String](
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
      )
    } yield rootId

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

  private def planResolved(
      resolved: ResolvedEntity,
      stored: StoredState
  ): SyncResult[SyncPlan] =
    resolved match {
      case ResolvedWorkingGroup(id, title) =>
        val target = IconikWorkingGroup(id, title)
        val steps = planCollectionChange(
          "working_group",
          id,
          stored.workingGroup,
          Some(target)
        ) {
          Right(iconikStore.upsertWorkingGroup(target))
        }
        EitherT.rightT[Future, IconikSyncError](
          SyncPlan(id, "working_group", steps)
        )

      case ResolvedCommission(id, title, wgId, wgTitle, year) =>
        for {
          parentSteps <- EitherT.fromEither[Future](
            planWorkingGroupIfMissing(wgId, wgTitle)
          )
          target = IconikCommission(wgId, id, title, year)
          reparented = stored.commission.exists(_.workingGroupId != wgId)
          plan <-
            if (reparented) {
              planReparentedCommission(target, stored.commission)
            } else {
              val steps = planCollectionChange(
                "commission",
                id,
                stored.commission,
                Some(target)
              ) {
                Right(iconikStore.upsertCommission(target))
              }
              EitherT.rightT[Future, IconikSyncError](
                SyncPlan(id, "commission", steps)
              )
            }
        } yield plan.copy(steps = parentSteps ++ plan.steps)

      case ResolvedProject(
            id,
            title,
            commId,
            commTitle,
            wgId,
            wgTitle,
            commYear
          ) =>
        for {
          workingGroupSteps <- EitherT.fromEither[Future](
            planWorkingGroupIfMissing(wgId, wgTitle)
          )
          commissionSteps <- EitherT.fromEither[Future](
            planCommissionIfMissing(commId, commTitle, wgId, commYear)
          )
          plan <- planProjectAndAtoms(id, title, commId, wgId, stored.project)
        } yield plan.copy(steps =
          workingGroupSteps ++ commissionSteps ++ plan.steps
        )
    }

  private def planReparentedCommission(
      target: IconikCommission,
      existing: Option[IconikCommission]
  ): SyncResult[SyncPlan] =
    for {
      childProjects <- EitherT.fromEither[Future](
        liftStore(iconikStore.getProjectsByCommissionId(target.id))
      )
      staleProjects =
        childProjects.filter(_.workingGroupId != target.workingGroupId)
      staleProjectIds = staleProjects.map(_.id).toSet
      atoms <- linkedAtoms
      atomIdsPreventingSync = atoms
        .filter { atom =>
          atom.projectId.exists(staleProjectIds.contains) ||
          (atom.commissionId.contains(target.id) &&
            !atom.workingGroupId.contains(target.workingGroupId))
        }
        .map(_.atomId)
        .distinct
        .sorted
      _ <- EitherT.cond[Future][IconikSyncError, Unit](
        atomIdsPreventingSync.isEmpty,
        (),
        SyncCascadeBlocked(
          id = target.id,
          entityType = "commission",
          message =
            "Reparenting commission requires updating child projects and atoms; sync child projects first",
          blockingChildIds = staleProjects.map(_.id).sorted,
          atomIdsPreventingSync = atomIdsPreventingSync
        )
      )
    } yield {
      val childSteps = staleProjects.flatMap { project =>
        val updated = project.copy(workingGroupId = target.workingGroupId)
        planCollectionChange(
          "project",
          project.id,
          Some(project),
          Some(updated)
        ) {
          persistProject(Some(project), updated)
        }
      }
      val commissionSteps =
        planCollectionChange("commission", target.id, existing, Some(target)) {
          Right(iconikStore.upsertCommission(target))
        }
      SyncPlan(
        target.id,
        "commission",
        childSteps ++ commissionSteps
      )
    }

  private def planProjectAndAtoms(
      projectId: String,
      title: String,
      commissionId: String,
      workingGroupId: String,
      existingProject: Option[IconikProject]
  ): SyncResult[SyncPlan] =
    linkedAtoms.map { atoms =>
      val staleAtomRefs = atoms.filter { atom =>
        atom.projectId.contains(projectId) &&
        (!atom.commissionId.contains(commissionId) ||
          !atom.workingGroupId.contains(workingGroupId))
      }

      val atomChanges = staleAtomRefs.map { ref =>
        val change = IconikSyncChange(
          id = ref.atomId,
          entityType = "atom",
          action = "updated",
          fields = changedFields(
            Json.obj(
              "commissionId" -> ref.commissionId,
              "workingGroupId" -> ref.workingGroupId,
              "revision" -> ref.revision
            ),
            Json.obj(
              "commissionId" -> commissionId,
              "workingGroupId" -> workingGroupId,
              "revision" -> (ref.revision + 1)
            )
          ),
          isPublished = Some(ref.isPublished)
        )
        PendingChange(
          change,
          () =>
            atomStore
              .updateProjectParents(
                ref,
                projectId,
                commissionId,
                workingGroupId
              )
              .map(
                _.left
                  .map(SyncDynamoError)
                  .flatMap(updated =>
                    Either.cond(
                      updated,
                      Some(change),
                      SyncWriteConflict(ref.atomId, "atom")
                    )
                  )
              )
        )
      }
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
      val projectSteps = planCollectionChange(
        "project",
        projectId,
        existingProject,
        Some(target)
      ) {
        persistProject(existingProject, target)
      }
      SyncPlan(projectId, "project", List(atomChanges) ++ projectSteps)
    }

  private def planWorkingGroupIfMissing(
      wgId: String,
      wgTitle: String
  ): Either[IconikSyncError, List[List[PendingChange]]] =
    liftStore(iconikStore.getWorkingGroup(wgId)).map {
      case Some(_) => Nil
      case None =>
        val target = IconikWorkingGroup(wgId, wgTitle)
        planCollectionChange("working_group", wgId, None, Some(target)) {
          Right(iconikStore.upsertWorkingGroup(target))
        }
    }

  private def planCommissionIfMissing(
      commId: String,
      commTitle: String,
      wgId: String,
      year: Option[String]
  ): Either[IconikSyncError, List[List[PendingChange]]] =
    liftStore(iconikStore.getCommission(commId)).map {
      case Some(_) => Nil
      case None =>
        val target = IconikCommission(wgId, commId, commTitle, year)
        planCollectionChange("commission", commId, None, Some(target)) {
          Right(iconikStore.upsertCommission(target))
        }
    }

  private def planCollectionChange[A: Writes](
      entityType: String,
      id: String,
      before: Option[A],
      after: Option[A]
  )(write: => Either[IconikSyncError, Unit]): List[List[PendingChange]] =
    if (before == after) Nil
    else {
      def fields(value: Option[A]): JsObject =
        value.map(v => Json.toJson(v).as[JsObject]).getOrElse(Json.obj())

      val change = IconikSyncChange(
        id,
        entityType,
        if (before.isEmpty) "created"
        else if (after.isEmpty) "deleted"
        else "updated",
        changedFields(fields(before), fields(after))
      )
      List(
        List(
          PendingChange(
            change,
            () =>
              Future {
                write.map(_ => Some(change))
              }
          )
        )
      )
    }

  private def persistProject(
      before: Option[IconikProject],
      after: IconikProject
  ): Either[IconikSyncError, Unit] =
    liftStore(iconikStore.syncProject(before, after)).flatMap(updated =>
      Either.cond(updated, (), SyncWriteConflict(after.id, "project"))
    )

  private def changedFields(before: JsObject, after: JsObject): JsObject =
    JsObject((before.keys ++ after.keys).toSeq.sorted.flatMap { field =>
      val oldValue = before.value.getOrElse(field, JsNull)
      val newValue = after.value.getOrElse(field, JsNull)
      Option.when(oldValue != newValue)(
        field -> Json.obj("before" -> oldValue, "after" -> newValue)
      )
    })

  private def linkedAtoms: SyncResult[List[AtomIconikRef]] =
    EitherT(atomStore.listLinkedAtoms()).leftMap(SyncDynamoError)

  private def fetchCollection(
      id: String
  ): SyncResult[Option[IconikCollection]] =
    EitherT(iconikApiClient.getCollection(id, credentials))
      .leftMap(SyncIconikError)

  private def liftStore[A](
      result: Either[E, A]
  ): Either[IconikSyncError, A] =
    result.left.map(err => SyncDynamoError(DynamoFailure(err.toString)))

}
