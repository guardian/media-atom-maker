package com.gu.media.iconik

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsNull, JsObject, Json, OWrites, Writes}

import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

sealed trait IconikSyncAction {
  def value: String
}

object IconikSyncAction {
  case object Created extends IconikSyncAction { val value = "created" }
  case object Deleted extends IconikSyncAction { val value = "deleted" }
  case object Updated extends IconikSyncAction { val value = "updated" }
  case object Unchanged extends IconikSyncAction { val value = "unchanged" }

  implicit val writes: Writes[IconikSyncAction] =
    Writes(action => play.api.libs.json.JsString(action.value))
}

sealed trait IconikSyncEntityType {
  def value: String
}

object IconikSyncEntityType {
  case object WorkingGroup extends IconikSyncEntityType {
    val value = "working_group"
  }
  case object Commission extends IconikSyncEntityType {
    val value = "commission"
  }
  case object Project extends IconikSyncEntityType {
    val value = "project"
  }
  case object Atom extends IconikSyncEntityType { val value = "atom" }

  implicit val writes: Writes[IconikSyncEntityType] =
    Writes(entityType => play.api.libs.json.JsString(entityType.value))
}

case class IconikSyncChange(
    id: String,
    entityType: IconikSyncEntityType,
    action: IconikSyncAction,
    fields: JsObject,
    isPublished: Option[Boolean] = None
)

object IconikSyncChange {
  implicit val writes: OWrites[IconikSyncChange] = Json.writes[IconikSyncChange]
}

case class IconikSyncResult(
    id: String,
    entityType: IconikSyncEntityType,
    action: IconikSyncAction,
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

case class SyncWriteConflict(id: String, entityType: IconikSyncEntityType)
    extends IconikSyncError {
  val message =
    s"${entityType.value} $id changed during sync; preview and retry"
}

/** Sync cannot proceed while these child collections or atom references remain
  * unresolved.
  */
case class SyncCascadeBlocked(
    id: String,
    entityType: IconikSyncEntityType,
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
      changes: List[IconikSyncChange],
      run: () => Future[Either[IconikSyncError, List[IconikSyncChange]]]
  )

  /** Keeps preview read-only and orders writes to preserve hierarchy
    * references.
    */
  private case class SyncPlan(
      id: String,
      entityType: IconikSyncEntityType,
      steps: List[List[PendingChange]]
  ) {
    def changes: List[IconikSyncChange] = steps.flatten.flatMap(_.changes)

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
            if (changes.nonEmpty) IconikSyncAction.Updated
            else IconikSyncAction.Unchanged
          ),
        updatedChildIds = changes
          .filter(c =>
            c.entityType == IconikSyncEntityType.Project && c.id != id
          )
          .map(_.id)
          .distinct
          .sorted,
        updatedAtomIds = changes
          .filter(_.entityType == IconikSyncEntityType.Atom)
          .map(_.id)
          .distinct
          .sorted,
        dryRun = dryRun,
        changes = changes
      )
  }

  private val cachedLibraryRoot = new AtomicReference[Option[String]](None)
  private val maxHierarchyDepth = 6

  private sealed trait ResolvedEntity {
    def entityType: IconikSyncEntityType
  }
  private case class ResolvedWorkingGroup(id: String, title: String)
      extends ResolvedEntity {
    val entityType = IconikSyncEntityType.WorkingGroup
  }
  private case class ResolvedCommission(
      id: String,
      title: String,
      workingGroupId: String,
      workingGroupTitle: String,
      year: Option[String]
  ) extends ResolvedEntity {
    val entityType = IconikSyncEntityType.Commission
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
    val entityType = IconikSyncEntityType.Project
  }

  private case class StoredState(
      project: Option[IconikProject],
      commission: Option[IconikCommission],
      workingGroup: Option[IconikWorkingGroup]
  ) {
    def storedType: Option[IconikSyncEntityType] =
      project
        .map(_ => IconikSyncEntityType.Project)
        .orElse(commission.map(_ => IconikSyncEntityType.Commission))
        .orElse(workingGroup.map(_ => IconikSyncEntityType.WorkingGroup))
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
              entityType = IconikSyncEntityType.Project,
              message =
                "Project is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          IconikSyncEntityType.Project,
          planCollectionChange(
            IconikSyncEntityType.Project,
            id,
            Some(project),
            None
          ) {
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
              entityType = IconikSyncEntityType.Commission,
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
              entityType = IconikSyncEntityType.Commission,
              message =
                "Commission is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          IconikSyncEntityType.Commission,
          planCollectionChange(
            IconikSyncEntityType.Commission,
            id,
            Some(commission),
            None
          ) {
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
              entityType = IconikSyncEntityType.WorkingGroup,
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
              entityType = IconikSyncEntityType.WorkingGroup,
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
              entityType = IconikSyncEntityType.WorkingGroup,
              message =
                "Working group is missing in Iconik but still referenced by atoms",
              atomIdsPreventingSync = atomIdsPreventingSync
            )
          )
        } yield SyncPlan(
          id,
          IconikSyncEntityType.WorkingGroup,
          planCollectionChange(
            IconikSyncEntityType.WorkingGroup,
            id,
            Some(workingGroup),
            None
          ) {
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
              s"Collection $id resolved as ${resolved.entityType.value} in Iconik, conflicting with stored ${existingType.value}"
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
          IconikSyncEntityType.WorkingGroup,
          id,
          stored.workingGroup,
          Some(target)
        ) {
          Right(iconikStore.upsertWorkingGroup(target))
        }
        EitherT.rightT[Future, IconikSyncError](
          SyncPlan(id, IconikSyncEntityType.WorkingGroup, steps)
        )

      case ResolvedCommission(id, title, wgId, wgTitle, year) =>
        for {
          parentSteps <- EitherT.fromEither[Future](
            planWorkingGroupIfMissing(wgId, wgTitle)
          )
          target = IconikCommission(wgId, id, title, year)
          plan <- planCommission(target, stored.commission)
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

  private def planCommission(
      target: IconikCommission,
      existing: Option[IconikCommission]
  ): SyncResult[SyncPlan] =
    for {
      childProjects <- EitherT.fromEither[Future](
        liftStore(iconikStore.getProjectsByCommissionId(target.id))
      )
      childProjectIds = childProjects.map(_.id).toSet
      staleProjects =
        childProjects.filter(_.workingGroupId != target.workingGroupId)
      staleProjectIds = staleProjects.map(_.id).toSet
      atoms <- linkedAtoms
      blockingAtoms = atoms.filter { atom =>
        (atom.projectId.exists(staleProjectIds.contains) &&
          (!atom.commissionId.contains(target.id) ||
            !atom.workingGroupId.contains(target.workingGroupId))) ||
        (atom.commissionId.contains(target.id) &&
          !atom.workingGroupId.contains(target.workingGroupId))
      }
      atomIdsPreventingSync = blockingAtoms.map(_.atomId).distinct.sorted
      blockingChildIds =
        (staleProjectIds ++ blockingAtoms
          .flatMap(_.projectId)
          .filter(childProjectIds.contains)).toList.sorted
      _ <- EitherT.cond[Future][IconikSyncError, Unit](
        atomIdsPreventingSync.isEmpty,
        (),
        SyncCascadeBlocked(
          id = target.id,
          entityType = IconikSyncEntityType.Commission,
          message =
            "Reparenting commission requires updating child projects and atoms; sync child projects first",
          blockingChildIds = blockingChildIds,
          atomIdsPreventingSync = atomIdsPreventingSync
        )
      )
    } yield {
      val childSteps = staleProjects.flatMap { project =>
        val updated = project.copy(workingGroupId = target.workingGroupId)
        planCollectionChange(
          IconikSyncEntityType.Project,
          project.id,
          Some(project),
          Some(updated)
        ) {
          persistProject(Some(project), updated)
        }
      }
      val commissionSteps =
        planCollectionChange(
          IconikSyncEntityType.Commission,
          target.id,
          existing,
          Some(target)
        ) {
          Right(iconikStore.upsertCommission(target))
        }
      SyncPlan(
        target.id,
        IconikSyncEntityType.Commission,
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
      val staleAtomIds = atoms
        .filter { atom =>
          atom.projectId.contains(projectId) &&
          (!atom.commissionId.contains(commissionId) ||
            !atom.workingGroupId.contains(workingGroupId))
        }
        .map(_.atomId)
        .toSet

      val atomChanges = atoms
        .filter(ref => staleAtomIds.contains(ref.atomId))
        .groupBy(_.atomId)
        .toList
        .sortBy(_._1)
        .map { case (atomId, atomRefs) =>
          val refs = atomRefs.sortBy(_.isPublished)
          val changes = refs.map { ref =>
            val updateParents = ref.projectId.contains(projectId)
            IconikSyncChange(
              id = ref.atomId,
              entityType = IconikSyncEntityType.Atom,
              action = IconikSyncAction.Updated,
              fields = changedFields(
                Json.obj(
                  "commissionId" -> ref.commissionId,
                  "workingGroupId" -> ref.workingGroupId,
                  "revision" -> ref.revision
                ),
                Json.obj(
                  "commissionId" -> (
                    if (updateParents) Some(commissionId) else ref.commissionId
                  ),
                  "workingGroupId" -> (
                    if (updateParents) Some(workingGroupId)
                    else ref.workingGroupId
                  ),
                  "revision" -> (ref.revision + 1)
                )
              ),
              isPublished = Some(ref.isPublished)
            )
          }
          PendingChange(
            changes,
            () =>
              atomStore
                .updateProjectParents(
                  refs,
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
                        changes,
                        SyncWriteConflict(
                          atomId,
                          IconikSyncEntityType.Atom
                        )
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
        IconikSyncEntityType.Project,
        projectId,
        existingProject,
        Some(target)
      ) {
        persistProject(existingProject, target)
      }
      SyncPlan(
        projectId,
        IconikSyncEntityType.Project,
        List(atomChanges) ++ projectSteps
      )
    }

  private def planWorkingGroupIfMissing(
      wgId: String,
      wgTitle: String
  ): Either[IconikSyncError, List[List[PendingChange]]] =
    liftStore(iconikStore.getWorkingGroup(wgId)).map {
      case Some(_) => Nil
      case None =>
        val target = IconikWorkingGroup(wgId, wgTitle)
        planCollectionChange(
          IconikSyncEntityType.WorkingGroup,
          wgId,
          None,
          Some(target)
        ) {
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
        planCollectionChange(
          IconikSyncEntityType.Commission,
          commId,
          None,
          Some(target)
        ) {
          Right(iconikStore.upsertCommission(target))
        }
    }

  private def planCollectionChange[A: Writes](
      entityType: IconikSyncEntityType,
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
        if (before.isEmpty) IconikSyncAction.Created
        else if (after.isEmpty) IconikSyncAction.Deleted
        else IconikSyncAction.Updated,
        changedFields(fields(before), fields(after))
      )
      List(
        List(
          PendingChange(
            List(change),
            () =>
              Future {
                write.map(_ => List(change))
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
      Either.cond(
        updated,
        (),
        SyncWriteConflict(after.id, IconikSyncEntityType.Project)
      )
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
