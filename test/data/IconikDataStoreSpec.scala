package data

import com.gu.media.iconik.{
  IconikCommission,
  IconikDynamoProjectStore,
  IconikInMemoryStore,
  IconikInMemoryStoreWithParentIndex,
  IconikInMemoryProjectStore,
  IconikProject,
  IconikStore,
  IconikUpsertRequest,
  IconikWorkingGroup,
  IndexConfig
}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scanamo.Scanamo
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  ConditionalCheckFailedException,
  PutItemRequest,
  UpdateItemRequest,
  UpdateItemResponse
}
import util.AWSConfig

import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

class IconikDataStoreSpec extends AnyFlatSpec with Matchers {

  behavior of "IconikStore"

  it should "make calls to each of the underlying data stores in response to an upsert request" in {
    val projectStore =
      new IconikInMemoryProjectStore[Nothing]()
    val commissionStore =
      new IconikInMemoryStoreWithParentIndex[IconikCommission, Nothing]()
    val workingGroupStore =
      new IconikInMemoryStore[IconikWorkingGroup, Nothing]()

    val store = new IconikStore[Nothing](
      projectStore,
      commissionStore,
      workingGroupStore
    )

    val createdAt = LocalDateTime.now()

    store.upsertIconikData(
      IconikUpsertRequest(
        id = "proj1",
        title = "Project 1",
        commissionId = "comm1",
        commissionTitle = "Commission 1",
        workingGroupId = "wg1",
        workingGroupTitle = "Working Group 1",
        status = "active",
        masterPlaceholderId = Some("mp1"),
        yearName = Some("2026")
      ),
      createdAtDateTimeOverride = Some(createdAt)
    )
    projectStore.store should have size 1
    projectStore.store.head shouldEqual IconikProject(
      id = "proj1",
      title = "Project 1",
      status = "active",
      commissionId = "comm1",
      workingGroupId = "wg1",
      masterPlaceholderId = Some("mp1"),
      createdAt =
        Some(createdAt.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME))
    )
    commissionStore.store should have size 1
    commissionStore.store.head shouldEqual IconikCommission(
      workingGroupId = "wg1",
      id = "comm1",
      title = "Commission 1",
      year = Some("2026")
    )
    workingGroupStore.store should have size 1
    workingGroupStore.store.head shouldEqual IconikWorkingGroup(
      id = "wg1",
      title = "Working Group 1"
    )

    store.upsertIconikData(
      IconikUpsertRequest(
        id = "proj1",
        title = "Project 1-updated",
        commissionId = "comm1",
        commissionTitle = "Commission 1-updated",
        workingGroupId = "wg1",
        workingGroupTitle = "Working Group 1-updated",
        status = "active",
        masterPlaceholderId = Some("mp1-updated"),
        yearName = Some("2026")
      )
    )

    projectStore.store should have size 1
    projectStore.store.head shouldEqual IconikProject(
      id = "proj1",
      title = "Project 1-updated",
      status = "active",
      commissionId = "comm1",
      workingGroupId = "wg1",
      masterPlaceholderId = Some("mp1-updated"),
      createdAt =
        Some(createdAt.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME))
    )
    commissionStore.store should have size 1
    commissionStore.store.head shouldEqual IconikCommission(
      workingGroupId = "wg1",
      id = "comm1",
      title = "Commission 1-updated",
      year = Some("2026")
    )
    workingGroupStore.store should have size 1
    workingGroupStore.store.head shouldEqual IconikWorkingGroup(
      id = "wg1",
      title = "Working Group 1-updated"
    )
  }

  it should "conditionally update only project hierarchy fields and reject concurrent writes" in {
    val client = mock[DynamoDbClient]
    val aws = mock[AWSConfig]
    when(aws.scanamo).thenReturn(Scanamo(client))
    val store = new IconikDynamoProjectStore(
      aws,
      "projects",
      IndexConfig("commission-index", "commissionId")
    )
    val before = IconikProject(
      "proj-1",
      "Old",
      "CREATED",
      "wg-old",
      "comm-old",
      None,
      None
    )
    val after = before.copy(
      title = "New",
      workingGroupId = "wg-new",
      commissionId = "comm-new"
    )
    val attributes = Map(
      "id" -> "proj-1",
      "title" -> "New",
      "status" -> "PROCESSING",
      "workingGroupId" -> "wg-new",
      "commissionId" -> "comm-new",
      "masterPlaceholderId" -> "new-master"
    ).map { case (key, value) =>
      key -> AttributeValue.builder().s(value).build()
    }.asJava
    when(client.updateItem(any(classOf[UpdateItemRequest])))
      .thenReturn(UpdateItemResponse.builder().attributes(attributes).build())

    store.updateHierarchy(before, after) shouldBe Right(true)
    val updateCaptor = ArgumentCaptor.forClass(classOf[UpdateItemRequest])
    verify(client).updateItem(updateCaptor.capture())
    val request = updateCaptor.getValue
    request.expressionAttributeNames().values().asScala.toSet shouldBe
      Set("id", "title", "workingGroupId", "commissionId")
    request.conditionExpression() should include("attribute_exists")
    request
      .expressionAttributeValues()
      .values()
      .asScala
      .map(_.s())
      .toSet should contain allOf (
      "Old",
      "wg-old",
      "comm-old",
      "New",
      "wg-new",
      "comm-new"
    )

    val conflict =
      ConditionalCheckFailedException.builder().message("changed").build()
    when(client.updateItem(any(classOf[UpdateItemRequest])))
      .thenThrow(conflict)
    store.updateHierarchy(before, after) shouldBe Right(false)
    when(client.putItem(any(classOf[PutItemRequest]))).thenThrow(conflict)
    store.createIfAbsent(after) shouldBe Right(false)
    val putCaptor = ArgumentCaptor.forClass(classOf[PutItemRequest])
    verify(client).putItem(putCaptor.capture())
    putCaptor.getValue.conditionExpression() should include(
      "attribute_not_exists"
    )
  }

}
