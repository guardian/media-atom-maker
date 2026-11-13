package com.gu.media.iconik

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

import java.util.concurrent.atomic.AtomicInteger
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
    val updateCalls = new AtomicInteger()

    override def listLinkedAtoms()
        : Future[Either[DynamoFailure, List[AtomIconikRef]]] =
      Future.successful(Right(refs.toList))

    override def updateProjectParents(
        ref: AtomIconikRef,
        projectId: String,
        commissionId: String,
        workingGroupId: String
    ): Future[Either[DynamoFailure, Boolean]] = {
      updateCalls.incrementAndGet()
      val index = refs.indexWhere(r =>
        r.atomId == ref.atomId && r.isPublished == ref.isPublished
      )
      if (
        index >= 0 && refs(index).projectId
          .contains(projectId) && refs(index).revision == ref.revision
      ) {
        refs(index) = refs(index).copy(
          commissionId = Some(commissionId),
          workingGroupId = Some(workingGroupId),
          revision = ref.revision + 1
        )
        Future.successful(Right(true))
      } else {
        Future.successful(Right(false))
      }
    }
  }

  private class RecordingIconikStore(
      projects: IconikProjectStore[String],
      commissions: IconikDataStoreWithParentIndex[IconikCommission, String],
      groups: IconikDataStore[IconikWorkingGroup, String]
  ) extends IconikStore[String](projects, commissions, groups) {
    val writeCalls = new AtomicInteger()
    override def upsertWorkingGroup(group: IconikWorkingGroup): Unit = {
      writeCalls.incrementAndGet()
      super.upsertWorkingGroup(group)
    }
    override def upsertCommission(commission: IconikCommission): Unit = {
      writeCalls.incrementAndGet()
      super.upsertCommission(commission)
    }
    override def upsertProject(project: IconikProject): Unit = {
      writeCalls.incrementAndGet()
      super.upsertProject(project)
    }
    override def syncProject(
        before: Option[IconikProject],
        after: IconikProject
    ): Either[String, Boolean] = {
      val result = super.syncProject(before, after)
      if (result == Right(true)) writeCalls.incrementAndGet()
      result
    }
    override def deleteWorkingGroup(id: String): Unit = {
      writeCalls.incrementAndGet()
      super.deleteWorkingGroup(id)
    }
    override def deleteCommission(id: String): Unit = {
      writeCalls.incrementAndGet()
      super.deleteCommission(id)
    }
    override def deleteProject(id: String): Unit = {
      writeCalls.incrementAndGet()
      super.deleteProject(id)
    }
  }

  private def fixture(
      collections: Map[String, Option[IconikCollection]],
      initialWorkingGroups: List[IconikWorkingGroup] = List(
        IconikWorkingGroup("wg-1", "Podcasts")
      ),
      initialCommissions: List[IconikCommission] = Nil,
      initialProjects: List[IconikProject] = Nil,
      initialAtoms: List[AtomIconikRef] = Nil,
      atomStore: Option[InMemoryAtomStore] = None,
      onFetch: (String, IconikStore[String]) => Unit = (_, _) => (),
      onScan: IconikStore[String] => Unit = _ => ()
  ) = {
    val projects =
      new IconikInMemoryProjectStore[String]()
    val commissions =
      new IconikInMemoryStoreWithParentIndex[IconikCommission, String]()
    val groups = new IconikInMemoryStore[IconikWorkingGroup, String]()
    initialWorkingGroups.foreach(groups.upsert)
    initialCommissions.foreach(commissions.upsert)
    initialProjects.foreach(projects.upsert)

    val store = new RecordingIconikStore(projects, commissions, groups)
    val atoms = atomStore.getOrElse(new InMemoryAtomStore(initialAtoms) {
      override def listLinkedAtoms()
          : Future[Either[DynamoFailure, List[AtomIconikRef]]] = {
        onScan(store)
        super.listLinkedAtoms()
      }
    })
    val client = new IconikCollectionClient {
      override def getCollection(
          entityId: String,
          providedCredentials: IconikCredentials
      ): Future[Either[IconikRequestFailure, Option[IconikCollection]]] = {
        onFetch(entityId, store)
        Future.successful(Right(collections.getOrElse(entityId, None)))
      }
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

    val expected = IconikSyncResult(
      id = "proj-1",
      entityType = "project",
      action = "updated",
      updatedAtomIds = List("atom-1"),
      changes = initialAtoms.map(ref =>
        IconikSyncChange(
          "atom-1",
          "atom",
          "updated",
          Json.obj(
            "commissionId" -> Json
              .obj("before" -> "comm-old", "after" -> "comm-new"),
            "revision" -> Json.obj("before" -> 0, "after" -> 1)
          ),
          Some(ref.isPublished)
        )
      ) :+ IconikSyncChange(
        "proj-1",
        "project",
        "updated",
        Json.obj(
          "title" -> Json
            .obj("before" -> "Old Project", "after" -> "Renamed Project"),
          "commissionId" -> Json.obj(
            "before" -> "comm-old",
            "after" -> "comm-new"
          )
        )
      )
    )
    await(service.sync("proj-1", dryRun = true)) shouldBe Right(
      expected.copy(dryRun = true)
    )
    store.writeCalls.get() shouldBe 0
    atoms.updateCalls.get() shouldBe 0
    store.getProject("proj-1") shouldBe Right(Some(originalProject))
    atoms.refs.toList shouldBe initialAtoms

    await(service.sync("proj-1")) shouldBe Right(expected)
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
    await(service.sync("proj-1")) shouldBe Right(
      IconikSyncResult("proj-1", "project", "unchanged")
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
    await(service.sync("comm-1", dryRun = true)) shouldBe rejected
    store.writeCalls.get() shouldBe 0
    atoms.updateCalls.get() shouldBe 0
    rejected shouldBe Left(
      SyncCascadeBlocked(
        id = "comm-1",
        entityType = "commission",
        message =
          "Reparenting commission requires updating child projects and atoms; sync child projects first",
        blockingChildIds = List("proj-1"),
        atomIdsPreventingSync = List("atom-1")
      )
    )
    store.getCommission("comm-1").map(_.map(_.workingGroupId)) shouldBe Right(
      Some("wg-1")
    )

    await(service.sync("proj-1")).map(_.copy(changes = Nil)) shouldBe Right(
      IconikSyncResult(
        id = "proj-1",
        entityType = "project",
        action = "updated",
        updatedAtomIds = List("atom-1")
      )
    )
    atoms.refs.head.workingGroupId shouldBe Some("wg-2")

    await(service.sync("comm-1")).map(_.copy(changes = Nil)) shouldBe Right(
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

    val expected = IconikSyncResult(
      id = "comm-1",
      entityType = "commission",
      action = "updated",
      updatedChildIds = List("proj-1"),
      changes = List("proj-1" -> "project", "comm-1" -> "commission").map {
        case (id, entityType) =>
          IconikSyncChange(
            id,
            entityType,
            "updated",
            Json.obj(
              "workingGroupId" -> Json.obj(
                "before" -> "wg-1",
                "after" -> "wg-2"
              )
            )
          )
      }
    )
    await(service.sync("comm-1", dryRun = true)) shouldBe Right(
      expected.copy(dryRun = true)
    )
    store.writeCalls.get() shouldBe 0
    store.getProject("proj-1").map(_.map(_.workingGroupId)) shouldBe Right(
      Some("wg-1")
    )
    await(service.sync("comm-1")) shouldBe Right(expected)
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

    val deletionPreview =
      await(service.sync("proj-orphan", dryRun = true)).toOption.get
    deletionPreview.action shouldBe "deleted"
    (deletionPreview.changes.head.fields \ "title").get shouldBe
      Json.obj(
        "before" -> "Unused Project",
        "after" -> play.api.libs.json.JsNull
      )
    store.writeCalls.get() shouldBe 0
    store.getProject("proj-orphan").map(_.isDefined) shouldBe Right(true)
    await(service.sync("proj-orphan"))
      .map(_.copy(changes = Nil)) shouldBe Right(
      IconikSyncResult("proj-orphan", "project", "deleted")
    )
    val writesBeforePreview = store.writeCalls.get()
    await(service.sync("comm-orphan", dryRun = true))
      .map(_.action) shouldBe Right("deleted")
    store.writeCalls.get() shouldBe writesBeforePreview
    await(service.sync("comm-orphan"))
      .map(_.copy(changes = Nil)) shouldBe Right(
      IconikSyncResult("comm-orphan", "commission", "deleted")
    )
    store.getCommission("comm-orphan") shouldBe Right(None)

    await(service.sync("proj-peptides")) shouldBe Left(
      SyncCascadeBlocked(
        id = "proj-peptides",
        entityType = "project",
        message = "Project is missing in Iconik but still referenced by atoms",
        atomIdsPreventingSync = List("atom-peptides")
      )
    )
    store.getProject("proj-peptides").map(_.isDefined) shouldBe Right(true)
  }

  it should "preview missing parents and a new project without creating records" in {
    val collections = Map(
      "root" -> Some(IconikCollection("Library", None)),
      "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
      "wg-2" -> Some(IconikCollection("News", Some("root"))),
      "comm-new" -> Some(IconikCollection("New Commission", Some("wg-2"))),
      "proj-new" -> Some(IconikCollection("New Project", Some("comm-new")))
    )
    val (service, store, atoms) = fixture(collections)

    val preview = await(service.sync("proj-new", dryRun = true)).toOption.get

    preview.dryRun shouldBe true
    preview.action shouldBe "created"
    preview.changes.map(c => (c.id, c.entityType, c.action)) shouldBe List(
      ("wg-2", "working_group", "created"),
      ("comm-new", "commission", "created"),
      ("proj-new", "project", "created")
    )
    (preview.changes.head.fields \ "title").get shouldBe
      Json.obj("before" -> play.api.libs.json.JsNull, "after" -> "News")
    store.writeCalls.get() shouldBe 0
    atoms.updateCalls.get() shouldBe 0
    store.getWorkingGroup("wg-2") shouldBe Right(None)
    store.getCommission("comm-new") shouldBe Right(None)
    store.getProject("proj-new") shouldBe Right(None)

    val applied = await(service.sync("proj-new")).toOption.get
    applied.dryRun shouldBe false
    applied.changes.map(_.id) shouldBe preview.changes.map(_.id)
    store.writeCalls.get() shouldBe 3
    store.getProject("proj-new").map(_.map(_.commissionId)) shouldBe Right(
      Some("comm-new")
    )
  }

  it should "preview working-group updates and deletions without writes" in {
    val group = IconikWorkingGroup("wg-1", "Old Name")
    val (renameService, renameStore, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("New Name", Some("root")))
      ),
      initialWorkingGroups = List(group)
    )
    val preview = await(renameService.sync("wg-1", dryRun = true)).toOption.get
    preview.changes shouldBe List(
      IconikSyncChange(
        "wg-1",
        "working_group",
        "updated",
        Json.obj(
          "title" -> Json.obj("before" -> "Old Name", "after" -> "New Name")
        )
      )
    )
    renameStore.writeCalls.get() shouldBe 0
    renameStore.getWorkingGroup("wg-1") shouldBe Right(Some(group))
    await(renameService.sync("wg-1")) shouldBe Right(
      preview.copy(dryRun = false)
    )

    val (deleteService, deleteStore, _) =
      fixture(Map.empty, initialWorkingGroups = List(group))
    val deletion = await(deleteService.sync("wg-1", dryRun = true)).toOption.get
    deletion.action shouldBe "deleted"
    deleteStore.writeCalls.get() shouldBe 0
    deleteStore.getWorkingGroup("wg-1") shouldBe Right(Some(group))
    await(deleteService.sync("wg-1")) shouldBe Right(
      deletion.copy(dryRun = false)
    )
    deleteStore.getWorkingGroup("wg-1") shouldBe Right(None)
  }

  it should "report a conflict when an atom changes before repair" in {
    val project = IconikProject(
      "proj-1",
      "Project",
      "CREATED",
      "wg-1",
      "comm-1",
      None,
      None
    )
    val ref = AtomIconikRef(
      "atom-1",
      false,
      false,
      Some("wg-1"),
      Some("old-comm"),
      Some("proj-1")
    )
    val atoms = new InMemoryAtomStore(List(ref)) {
      override def updateProjectParents(
          ref: AtomIconikRef,
          projectId: String,
          commissionId: String,
          workingGroupId: String
      ): Future[Either[DynamoFailure, Boolean]] =
        Future.successful(Right(false))
    }
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-1"))),
        "proj-1" -> Some(IconikCollection("Project", Some("comm-1")))
      ),
      initialCommissions =
        List(IconikCommission("wg-1", "comm-1", "Commission", None)),
      initialProjects = List(project),
      atomStore = Some(atoms)
    )

    val preview = await(service.sync("proj-1", dryRun = true)).toOption.get
    preview.updatedAtomIds shouldBe List("atom-1")
    preview.changes.map(_.entityType) shouldBe List("atom")
    await(service.sync("proj-1")) shouldBe Left(
      SyncWriteConflict("atom-1", "atom")
    )
    store.writeCalls.get() shouldBe 0
  }

  it should "stop before changing the project when an atom repair fails" in {
    val project = IconikProject(
      "proj-1",
      "Old title",
      "CREATED",
      "wg-1",
      "comm-1",
      None,
      None
    )
    val ref = AtomIconikRef(
      "atom-1",
      false,
      false,
      Some("wg-1"),
      Some("old-comm"),
      Some("proj-1")
    )
    val failure = DynamoFailure("Atom repair failed")
    val atoms = new InMemoryAtomStore(List(ref)) {
      override def updateProjectParents(
          ref: AtomIconikRef,
          projectId: String,
          commissionId: String,
          workingGroupId: String
      ): Future[Either[DynamoFailure, Boolean]] =
        Future.successful(Left(failure))
    }
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-1"))),
        "proj-1" -> Some(IconikCollection("New title", Some("comm-1")))
      ),
      initialCommissions =
        List(IconikCommission("wg-1", "comm-1", "Commission", None)),
      initialProjects = List(project),
      atomStore = Some(atoms)
    )

    await(service.sync("proj-1")) shouldBe Left(SyncDynamoError(failure))
    store.writeCalls.get() shouldBe 0
    store.getProject("proj-1") shouldBe Right(Some(project))
  }

  it should "clear a stored year when the commission moves directly under its working group" in {
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-1")))
      ),
      initialCommissions =
        List(IconikCommission("wg-1", "comm-1", "Commission", Some("2025")))
    )

    val preview = await(service.sync("comm-1", dryRun = true)).toOption.get
    (preview.changes.head.fields \ "year").get shouldBe
      Json.obj("before" -> "2025", "after" -> play.api.libs.json.JsNull)
    store.getCommission("comm-1").map(_.flatMap(_.year)) shouldBe Right(
      Some("2025")
    )
    await(service.sync("comm-1")) shouldBe Right(preview.copy(dryRun = false))
    store.getCommission("comm-1").map(_.flatMap(_.year)) shouldBe Right(None)
  }

  it should "prevent working-group deletion when a project directly references it" in {
    val project = IconikProject(
      "proj-1",
      "Project",
      "CREATED",
      "old-wg",
      "comm-1",
      None,
      None
    )
    val (service, store, _) = fixture(
      Map.empty,
      initialWorkingGroups = List(
        IconikWorkingGroup("old-wg", "Old"),
        IconikWorkingGroup("new-wg", "New")
      ),
      initialCommissions =
        List(IconikCommission("new-wg", "comm-1", "Commission", None)),
      initialProjects = List(project)
    )
    List(true, false).foreach { dryRun =>
      await(service.sync("old-wg", dryRun)) shouldBe Left(
        SyncCascadeBlocked(
          "old-wg",
          "working_group",
          "Working group is still referenced by projects; sync those projects first",
          blockingChildIds = List("proj-1")
        )
      )
    }
    store.writeCalls.get() shouldBe 0
    store.getWorkingGroup("old-wg").map(_.isDefined) shouldBe Right(true)
    store.getProject("proj-1") shouldBe Right(Some(project))
  }

  it should "preserve project metadata updated concurrently with hierarchy planning" in {
    val project = IconikProject(
      "proj-1",
      "Old title",
      "CREATED",
      "wg-1",
      "comm-1",
      None,
      None
    )
    val newer = project.copy(
      status = "PROCESSING",
      masterPlaceholderId = Some("new-master")
    )
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-1"))),
        "proj-1" -> Some(IconikCollection("New title", Some("comm-1")))
      ),
      initialCommissions =
        List(IconikCommission("wg-1", "comm-1", "Commission", None)),
      initialProjects = List(project),
      onFetch =
        (id, current) => if (id == "proj-1") current.upsertProject(newer)
    )

    await(service.sync("proj-1")).map(_.action) shouldBe Right("updated")
    store.getProject("proj-1") shouldBe Right(
      Some(newer.copy(title = "New title"))
    )
  }

  it should "stop commission reparenting if a child project changes commission during the scan" in {
    val project = IconikProject(
      "proj-1",
      "Project",
      "CREATED",
      "wg-1",
      "comm-1",
      None,
      None
    )
    val moved = project.copy(commissionId = "other-commission")
    val commission = IconikCommission("wg-1", "comm-1", "Commission", None)
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "wg-2" -> Some(IconikCollection("News", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-2")))
      ),
      initialWorkingGroups = List(
        IconikWorkingGroup("wg-1", "Podcasts"),
        IconikWorkingGroup("wg-2", "News")
      ),
      initialCommissions = List(commission),
      initialProjects = List(project),
      onScan = _.upsertProject(moved)
    )

    await(service.sync("comm-1")) shouldBe Left(
      SyncWriteConflict("proj-1", "project")
    )
    store.getProject("proj-1") shouldBe Right(Some(moved))
    store.getCommission("comm-1") shouldBe Right(Some(commission))
  }

  it should "not replace a project created after the initial read" in {
    val concurrent = IconikProject(
      "proj-1",
      "Ingested",
      "PROCESSING",
      "wg-1",
      "comm-1",
      Some("master"),
      None
    )
    val (service, store, _) = fixture(
      Map(
        "root" -> Some(IconikCollection("Library", None)),
        "wg-1" -> Some(IconikCollection("Podcasts", Some("root"))),
        "comm-1" -> Some(IconikCollection("Commission", Some("wg-1"))),
        "proj-1" -> Some(IconikCollection("Project", Some("comm-1")))
      ),
      initialCommissions =
        List(IconikCommission("wg-1", "comm-1", "Commission", None)),
      onFetch =
        (id, current) => if (id == "proj-1") current.upsertProject(concurrent)
    )

    await(service.sync("proj-1")) shouldBe Left(
      SyncWriteConflict("proj-1", "project")
    )
    store.getProject("proj-1") shouldBe Right(Some(concurrent))
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
