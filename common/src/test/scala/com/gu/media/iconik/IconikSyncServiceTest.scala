package com.gu.media.iconik

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class IconikSyncServiceTest extends AnyFlatSpec with Matchers {
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.global
  private val credentials = IconikCredentials("test-app", "test-token")

  private class InMemoryAtomStore(initial: List[AtomIconikRef] = Nil)
      extends IconikAtomStore {
    val refs: ListBuffer[AtomIconikRef] = ListBuffer.from(initial)

    override def listLinkedAtoms()
        : Future[Either[DynamoFailure, List[AtomIconikRef]]] =
      Future.successful(Right(refs.toList))

    override def updateProjectParents(
        ref: AtomIconikRef,
        projectId: String,
        commissionId: String,
        workingGroupId: String
    ): Future[Either[DynamoFailure, Boolean]] = {
      val index = refs.indexWhere(r =>
        r.atomId == ref.atomId && r.isPublished == ref.isPublished
      )
      if (index >= 0 && refs(index).projectId.contains(projectId)) {
        refs(index) = refs(index).copy(
          commissionId = Some(commissionId),
          workingGroupId = Some(workingGroupId)
        )
        Future.successful(Right(true))
      } else {
        Future.successful(Right(false))
      }
    }
  }

  private def fixture(
      collections: Map[String, Option[IconikCollection]],
      initialWorkingGroups: List[IconikWorkingGroup] = List(
        IconikWorkingGroup("wg-1", "Podcasts")
      ),
      initialCommissions: List[IconikCommission] = Nil,
      initialProjects: List[IconikProject] = Nil,
      initialAtoms: List[AtomIconikRef] = Nil
  ) = {
    val projects =
      new IconikInMemoryStoreWithParentIndex[IconikProject, String]()
    val commissions =
      new IconikInMemoryStoreWithParentIndex[IconikCommission, String]()
    val groups = new IconikInMemoryStore[IconikWorkingGroup, String]()
    initialWorkingGroups.foreach(groups.upsert)
    initialCommissions.foreach(commissions.upsert)
    initialProjects.foreach(projects.upsert)

    val store = new IconikStore[String](projects, commissions, groups)
    val atoms = new InMemoryAtomStore(initialAtoms)
    val client = new IconikCollectionClient {
      override def getCollection(
          entityId: String,
          providedCredentials: IconikCredentials
      ): Future[Either[IconikRequestFailure, Option[IconikCollection]]] =
        Future.successful(Right(collections.getOrElse(entityId, None)))
    }
    val service = new IconikSyncService(store, atoms, client, credentials)
    (service, store, atoms)
  }

  private def await[A](future: Future[A]): A = Await.result(future, 5.seconds)

  "IconikSyncService" should "reparent a project, update preview and published atoms, and preserve project metadata" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Multimedia Production", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
      "year-2026" -> Some(IconikCollection("2026", Some("wg-1"))),
      "comm-new" -> Some(
        IconikCollection("Science Weekly", Some("year-2026"))
      ),
      "proj-1" -> Some(IconikCollection("Renamed Project", Some("comm-new")))
    )
    val originalProject = IconikProject(
      id = "proj-1",
      title = "Old Project",
      status = "CREATED",
      workingGroupId = "wg-1",
      commissionId = "comm-old",
      masterPlaceholderId = Some("master-1"),
      createdAt = Some("2026-08-01T10:00:00")
    )
    val initialAtoms = List(
      AtomIconikRef(
        "atom-1",
        isPublished = false,
        isLegacyEncoding = false,
        Some("wg-1"),
        Some("comm-old"),
        Some("proj-1")
      ),
      AtomIconikRef(
        "atom-1",
        isPublished = true,
        isLegacyEncoding = false,
        Some("wg-1"),
        Some("comm-old"),
        Some("proj-1")
      )
    )

    val (service, store, atoms) = fixture(
      collections = collections,
      initialCommissions = List(
        IconikCommission("wg-1", "comm-old", "Science Weekly", Some("2026")),
        IconikCommission("wg-1", "comm-new", "Science Weekly", Some("2026"))
      ),
      initialProjects = List(originalProject),
      initialAtoms = initialAtoms
    )

    val result = await(service.sync("proj-1"))

    result shouldBe Right(
      IconikSyncResult(
        id = "proj-1",
        entityType = "project",
        action = "updated",
        updatedAtomIds = List("atom-1")
      )
    )
    store.getProject("proj-1") shouldBe Right(
      Some(
        originalProject.copy(
          title = "Renamed Project",
          commissionId = "comm-new"
        )
      )
    )
    atoms.refs.map(_.commissionId).toList shouldBe List(
      Some("comm-new"),
      Some("comm-new")
    )
  }

  it should "reject reparenting a commission when child projects also have atoms, then succeed once projects sync first" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Multimedia Production", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
      "wg-2" -> Some(IconikCollection("News Features", Some("root"))),
      "comm-1" -> Some(IconikCollection("Commission 1", Some("wg-2"))),
      "proj-1" -> Some(IconikCollection("Project 1", Some("comm-1")))
    )
    val (service, store, atoms) = fixture(
      collections = collections,
      initialWorkingGroups = List(
        IconikWorkingGroup("wg-1", "Podcasts"),
        IconikWorkingGroup("wg-2", "News Features")
      ),
      initialCommissions = List(
        IconikCommission("wg-1", "comm-1", "Commission 1", None)
      ),
      initialProjects = List(
        IconikProject(
          "proj-1",
          "Project 1",
          "CREATED",
          "wg-1",
          "comm-1",
          None,
          None
        )
      ),
      initialAtoms = List(
        AtomIconikRef(
          "atom-1",
          isPublished = false,
          isLegacyEncoding = false,
          Some("wg-1"),
          Some("comm-1"),
          Some("proj-1")
        )
      )
    )

    val rejected = await(service.sync("comm-1"))
    rejected shouldBe Left(
      SyncCascadeBlocked(
        id = "comm-1",
        entityType = "commission",
        message =
          "Reparenting commission requires updating child projects and atoms; sync child projects first",
        blockingChildIds = List("proj-1"),
        blockingAtomIds = List("atom-1")
      )
    )
    store.getCommission("comm-1").map(_.map(_.workingGroupId)) shouldBe Right(
      Some("wg-1")
    )

    await(service.sync("proj-1")) shouldBe Right(
      IconikSyncResult(
        id = "proj-1",
        entityType = "project",
        action = "updated",
        updatedAtomIds = List("atom-1")
      )
    )
    atoms.refs.head.workingGroupId shouldBe Some("wg-2")

    await(service.sync("comm-1")) shouldBe Right(
      IconikSyncResult(
        id = "comm-1",
        entityType = "commission",
        action = "updated"
      )
    )
    store.getCommission("comm-1").map(_.map(_.workingGroupId)) shouldBe Right(
      Some("wg-2")
    )
  }

  it should "update child projects one level down when a reparented commission has no atom-linked children" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Multimedia Production", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
      "wg-2" -> Some(IconikCollection("News Features", Some("root"))),
      "comm-1" -> Some(IconikCollection("Commission 1", Some("wg-2")))
    )
    val (service, store, _) = fixture(
      collections = collections,
      initialWorkingGroups = List(
        IconikWorkingGroup("wg-1", "Podcasts"),
        IconikWorkingGroup("wg-2", "News Features")
      ),
      initialCommissions = List(
        IconikCommission("wg-1", "comm-1", "Commission 1", None)
      ),
      initialProjects = List(
        IconikProject(
          "proj-1",
          "Project 1",
          "CREATED",
          "wg-1",
          "comm-1",
          None,
          None
        )
      )
    )

    await(service.sync("comm-1")) shouldBe Right(
      IconikSyncResult(
        id = "comm-1",
        entityType = "commission",
        action = "updated",
        updatedChildIds = List("proj-1")
      )
    )
    store.getProject("proj-1").map(_.map(_.workingGroupId)) shouldBe Right(
      Some("wg-2")
    )
  }

  it should "delete unreferenced 404 entities bottom-up and block deletion when children or atoms remain" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Multimedia Production", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root")))
    )
    val (service, store, _) = fixture(
      collections = collections,
      initialCommissions = List(
        IconikCommission("wg-1", "comm-orphan", "Duplicate Podcast", None),
        IconikCommission("wg-1", "comm-peptides", "Peptides", None)
      ),
      initialProjects = List(
        IconikProject(
          "proj-orphan",
          "Unused Project",
          "CREATED",
          "wg-1",
          "comm-orphan",
          None,
          None
        ),
        IconikProject(
          "proj-peptides",
          "Peptides OTG",
          "CREATED",
          "wg-1",
          "comm-peptides",
          None,
          None
        )
      ),
      initialAtoms = List(
        AtomIconikRef(
          "atom-peptides",
          isPublished = false,
          isLegacyEncoding = false,
          Some("wg-1"),
          Some("comm-peptides"),
          Some("proj-peptides")
        )
      )
    )

    await(service.sync("comm-orphan")) shouldBe Left(
      SyncCascadeBlocked(
        id = "comm-orphan",
        entityType = "commission",
        message = "Commission has child projects; sync child projects first",
        blockingChildIds = List("proj-orphan")
      )
    )

    await(service.sync("proj-orphan")) shouldBe Right(
      IconikSyncResult("proj-orphan", "project", "deleted")
    )
    await(service.sync("comm-orphan")) shouldBe Right(
      IconikSyncResult("comm-orphan", "commission", "deleted")
    )
    store.getCommission("comm-orphan") shouldBe Right(None)

    await(service.sync("proj-peptides")) shouldBe Left(
      SyncCascadeBlocked(
        id = "proj-peptides",
        entityType = "project",
        message = "Project is missing in Iconik but still referenced by atoms",
        blockingAtomIds = List("atom-peptides")
      )
    )
    store.getProject("proj-peptides").map(_.isDefined) shouldBe Right(true)
  }

  it should "reject collections outside the Iconik Library root" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Multimedia Production", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
      "mapped-root" -> Some(IconikCollection("LucidLink Storage", None)),
      "mapped-folder" -> Some(
        IconikCollection("Outside Folder", Some("mapped-root"))
      )
    )
    val (service, _, _) = fixture(collections)

    await(service.sync("mapped-folder")) shouldBe Left(
      SyncInvalidHierarchy(
        "mapped-root",
        "Collection is outside the Iconik Library root"
      )
    )
  }
}
