package com.gu.media.iconik

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scanamo.DynamoReadError
import play.api.libs.json.Json

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

class IconikValidationServiceTest extends AnyFlatSpec with Matchers {
  private implicit val executionContext: ExecutionContext =
    ExecutionContext.global

  "IconikValidationService" should "validate only the requested entity" in {
    val project = IconikProject(
      id = "project-1",
      title = "Stored project",
      status = "active",
      workingGroupId = "working-group-1",
      commissionId = "commission-1",
      masterPlaceholderId = None,
      createdAt = None
    )
    val commission = IconikCommission(
      workingGroupId = "working-group-1",
      id = "commission-1",
      title = "Stored commission"
    )
    val workingGroup =
      IconikWorkingGroup("working-group-1", "Stored working group")
    val projectStore =
      new IconikInMemoryStoreWithParentIndex[IconikProject, DynamoReadError]()
    val commissionStore = new IconikInMemoryStoreWithParentIndex[
      IconikCommission,
      DynamoReadError
    ]()
    val workingGroupStore =
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    projectStore.upsert(project)
    commissionStore.upsert(commission)
    workingGroupStore.upsert(workingGroup)
    val store =
      new IconikStore(projectStore, commissionStore, workingGroupStore)
    val credentials = IconikCredentials("test-app", "test-token")
    var requestedIds = List.empty[String]
    val client = new IconikCollectionClient {
      override def getCollection(
          entityId: String,
          providedCredentials: IconikCredentials
      ) = {
        requestedIds = requestedIds :+ entityId
        Future.successful(
          entityId match {
            case "project-1" =>
              Right(
                Some(
                  IconikCollection(
                    "Renamed project",
                    Some("other-commission")
                  )
                )
              )
            case "commission-1" => Right(None)
            case "working-group-1" =>
              Right(
                Some(
                  IconikCollection(
                    "Stored working group",
                    Some("iconik-parent")
                  )
                )
              )
          }
        )
      }
    }

    val service = new IconikValidationService(store, client, credentials)
    Await.result(service.listEntityIds(), 5.seconds) shouldBe Right(
      List(
        IconikEntityId("project", "project-1"),
        IconikEntityId("commission", "commission-1"),
        IconikEntityId("working_group", "working-group-1")
      )
    )
    val result = Await.result(
      service.validate(IconikEntityType.Project, "project-1"),
      5.seconds
    )

    result.map(_.toJson) shouldBe Right(
      Json.obj(
        "entities" -> Json.arr(
          Json.obj(
            "entityType" -> "project",
            "id" -> "project-1",
            "exists" -> true,
            "nameMatches" -> false,
            "parentMatches" -> false,
            "expectedName" -> "Stored project",
            "iconikName" -> "Renamed project",
            "expectedParentId" -> "commission-1",
            "iconikParentId" -> "other-commission"
          )
        ),
        "summary" -> Json.obj(
          "total" -> 1,
          "missing" -> 0,
          "nameMismatches" -> 1,
          "parentMismatches" -> 1
        )
      )
    )
    requestedIds shouldBe List("project-1")
  }

  it should "not query Iconik when the requested Dynamo entity is missing" in {
    val projectStore =
      new IconikInMemoryStoreWithParentIndex[IconikProject, DynamoReadError]()
    val commissionStore = new IconikInMemoryStoreWithParentIndex[
      IconikCommission,
      DynamoReadError
    ]()
    val workingGroupStore =
      new IconikInMemoryStore[IconikWorkingGroup, DynamoReadError]()
    var queriedIconik = false
    val client = new IconikCollectionClient {
      override def getCollection(
          entityId: String,
          providedCredentials: IconikCredentials
      ) = {
        queriedIconik = true
        Future.successful(Right(None))
      }
    }
    val service = new IconikValidationService(
      new IconikStore(projectStore, commissionStore, workingGroupStore),
      client,
      IconikCredentials("test-app", "test-token")
    )

    Await.result(
      service.validate(IconikEntityType.Project, "missing"),
      5.seconds
    ) shouldBe Left(IconikEntityNotFound("project", "missing"))
    queriedIconik shouldBe false
  }

  "IconikValidationService.validateEntity" should "report a missing collection" in {
    val entity = IconikEntity(
      entityType = "commission",
      id = "commission-1",
      name = "Stored commission",
      parentId = Some("working-group-1")
    )

    IconikValidationService.validateEntity(entity, None) shouldBe
      IconikEntityValidationResult(
        entityType = "commission",
        id = "commission-1",
        exists = false,
        nameMatches = None,
        parentMatches = None,
        expectedName = "Stored commission",
        iconikName = None,
        expectedParentId = Some("working-group-1"),
        iconikParentId = None
      )
  }

  it should "compare name and parent when the collection exists" in {
    val project = IconikEntity(
      entityType = "project",
      id = "project-1",
      name = "Stored project",
      parentId = Some("commission-1")
    )

    IconikValidationService.validateEntity(
      project,
      Some(IconikCollection("Stored project", Some("commission-1")))
    ) shouldBe
      IconikEntityValidationResult(
        entityType = "project",
        id = "project-1",
        exists = true,
        nameMatches = Some(true),
        parentMatches = Some(true),
        expectedName = "Stored project",
        iconikName = Some("Stored project"),
        expectedParentId = Some("commission-1"),
        iconikParentId = Some("commission-1")
      )

    IconikValidationService.validateEntity(
      project,
      Some(IconikCollection("Renamed project", Some("other-commission")))
    ) shouldBe
      IconikEntityValidationResult(
        entityType = "project",
        id = "project-1",
        exists = true,
        nameMatches = Some(false),
        parentMatches = Some(false),
        expectedName = "Stored project",
        iconikName = Some("Renamed project"),
        expectedParentId = Some("commission-1"),
        iconikParentId = Some("other-commission")
      )
  }

  it should "leave parentMatches empty when no parent is expected" in {
    val workingGroup = IconikEntity(
      entityType = "working_group",
      id = "working-group-1",
      name = "Stored working group",
      parentId = None
    )

    IconikValidationService.validateEntity(
      workingGroup,
      Some(IconikCollection("Stored working group", Some("iconik-parent")))
    ) shouldBe
      IconikEntityValidationResult(
        entityType = "working_group",
        id = "working-group-1",
        exists = true,
        nameMatches = Some(true),
        parentMatches = None,
        expectedName = "Stored working group",
        iconikName = Some("Stored working group"),
        expectedParentId = None,
        iconikParentId = Some("iconik-parent")
      )
  }

  "IconikCredentials.fromSecret" should "reject secrets without a token" in {
    intercept[IllegalStateException] {
      IconikCredentials.fromSecret("""{"app-id":"test-app"}""")
    }.getMessage should include("missing token")
  }
}
