package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.iconik.{
  AtomIconikRef,
  DynamoFailure,
  DynamoIconikAtomStore,
  IconikRequestFailure,
  IconikSyncChange,
  IconikSyncResult,
  IconikSyncService,
  SyncCascadeBlocked,
  SyncDynamoError,
  SyncIconikError,
  SyncInvalidHierarchy,
  SyncWriteConflict,
  SyncNotFound
}
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import com.gu.pandomainauth.model.{
  AuthenticatedUser,
  CookieSettings,
  OAuthSettings,
  PanDomainAuthSettings,
  User
}
import com.gu.pandomainauth.service.{CryptoConf, KeyPair}
import com.typesafe.config.ConfigFactory
import data.DataStores
import org.mockito.{ArgumentCaptor, Mockito}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, verifyZeroInteractions, when}
import org.scanamo.DynamoReadError
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.{
  AttributeValue,
  ConditionalCheckFailedException,
  ScanRequest,
  ScanResponse,
  UpdateItemRequest,
  UpdateItemResponse
}
import util.AWSConfig

import java.security.KeyPairGenerator
import java.time.{Clock, Instant, ZoneOffset}
import java.util.concurrent.CompletableFuture
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class IconikSyncControllerTest extends AnyFlatSpec with Matchers {
  private val controllerComponents = Helpers.stubControllerComponents()
  private val permissions = mock[MediaAtomMakerPermissionsProvider]
  private val settings = mock[PanDomainAuthSettingsRefresher]
  private val authWsClient = mock[WSClient]
  private val awsConfig = mock[AWSConfig]
  private val stores = mock[DataStores]
  when(awsConfig.stage).thenReturn("CODE")
  private val browserUser =
    User("Test", "Editor", "test.editor@guardian.co.uk", None)
  private val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()

  when(settings.system).thenReturn("video")
  when(settings.domain).thenReturn("example.test")
  when(settings.settings).thenReturn(
    PanDomainAuthSettings(
      CryptoConf.SigningAndVerification(
        KeyPair(keyPair.getPublic, keyPair.getPrivate),
        Nil
      ),
      CookieSettings("test-panda"),
      OAuthSettings(
        "test-client",
        "test-secret",
        "https://oauth.example.test",
        None
      ),
      None
    )
  )
  when(permissions.hasPermission(any(), any())).thenReturn(true)

  private val authActions = new PanDomainAuthActions {
    override protected val clock: Clock =
      Clock.fixed(Instant.now(), ZoneOffset.UTC)
    override def conf: Configuration =
      Configuration("secret" -> "test-secret", "host" -> "example.test")
    override def wsClient: WSClient = authWsClient
    override def controllerComponents: ControllerComponents =
      IconikSyncControllerTest.this.controllerComponents
    override val panDomainSettings: PanDomainAuthSettingsRefresher = settings
    override def permissionsProvider: MediaAtomMakerPermissionsProvider =
      IconikSyncControllerTest.this.permissions
  }

  private def controller(
      service: IconikSyncService[DynamoReadError],
      aws: AWSConfig = awsConfig
  ): IconikController =
    new IconikController(
      ConfigFactory.empty(),
      aws,
      authActions,
      stores,
      service,
      controllerComponents
    )

  private def callSync(
      controller: IconikController,
      id: String = "proj-1",
      validUser: Boolean = true,
      method: String = POST
  ) = {
    val user =
      if (validUser) browserUser
      else browserUser.copy(email = "test.editor@example.test")
    val cookie = authActions.generateCookie(
      AuthenticatedUser(
        user,
        "video",
        Set("video"),
        Instant.now().plusSeconds(3600),
        multiFactor = true
      )
    )
    val request: FakeRequest[AnyContent] =
      FakeRequest(method, s"/api/iconik/collections/$id/sync")
        .withCookies(cookie)

    controller.syncCollection(id, dryRun = method == GET).apply(request)
  }

  "syncCollection" should "return the sync result on success" in {
    val service = mock[IconikSyncService[DynamoReadError]]
    val syncResult = IconikSyncResult(
      id = "proj-1",
      entityType = "project",
      action = "updated",
      updatedAtomIds = List("atom-1")
    )
    when(service.sync("proj-1"))
      .thenReturn(Future.successful(Right(syncResult)))

    val result = callSync(controller(service))

    status(result) shouldBe OK
    contentAsJson(result) shouldBe syncResult.toJson
    routes.IconikController
      .syncCollection("proj-1", dryRun = false)
      .method shouldBe POST
  }

  it should "reject unauthorized callers before invoking the sync service" in {
    val service = mock[IconikSyncService[DynamoReadError]]

    List(GET, POST).foreach { method =>
      status(
        callSync(controller(service), validUser = false, method = method)
      ) shouldBe FORBIDDEN
    }
    verifyZeroInteractions(service)
  }

  it should "return a read-only preview for GET" in {
    val service = mock[IconikSyncService[DynamoReadError]]
    val preview = IconikSyncResult(
      "proj-1",
      "project",
      "updated",
      dryRun = true,
      changes = List(
        IconikSyncChange(
          "proj-1",
          "project",
          "updated",
          Json.obj(
            "title" -> Json.obj("before" -> "Old title", "after" -> "New title")
          )
        )
      )
    )
    when(service.sync("proj-1", dryRun = true))
      .thenReturn(Future.successful(Right(preview)))

    val result = callSync(controller(service), method = GET)

    status(result) shouldBe OK
    (contentAsJson(result) \ "dryRun").as[Boolean] shouldBe true
    (contentAsJson(result) \ "changes" \ 0 \ "fields" \ "title").get shouldBe
      Json.obj("before" -> "Old title", "after" -> "New title")
    Mockito.verify(service).sync("proj-1", dryRun = true)
    routes.IconikController
      .syncCollection("proj-1", dryRun = true)
      .method shouldBe GET
  }

  it should "reject DEV previews and writes before accessing the shared CODE collections" in {
    val service = mock[IconikSyncService[DynamoReadError]]
    val devAws = mock[AWSConfig]
    when(devAws.stage).thenReturn("DEV")
    List(GET, POST).foreach { method =>
      status(
        callSync(controller(service, devAws), method = method)
      ) shouldBe FORBIDDEN
    }
    verifyZeroInteractions(service)
  }

  it should "map domain and infrastructure errors to their HTTP statuses" in {
    val cases = Seq(
      SyncNotFound("missing") -> NOT_FOUND,
      SyncCascadeBlocked(
        "comm-1",
        "commission",
        "sync child projects first",
        blockingChildIds = List("proj-1"),
        atomIdsPreventingSync = List("atom-1")
      ) -> CONFLICT,
      SyncInvalidHierarchy("bad", "invalid hierarchy") -> UNPROCESSABLE_ENTITY,
      SyncWriteConflict("atom-1", "atom") -> CONFLICT,
      SyncIconikError(IconikRequestFailure("iconik 500")) -> BAD_GATEWAY,
      SyncDynamoError(DynamoFailure("dynamo down")) -> SERVICE_UNAVAILABLE
    )

    cases.foreach { case (error, expectedStatus) =>
      List(GET, POST).foreach { method =>
        val service = mock[IconikSyncService[DynamoReadError]]
        when(service.sync("proj-1", dryRun = method == GET))
          .thenReturn(Future.successful(Left(error)))

        val result = callSync(controller(service), method = method)

        status(result) shouldBe expectedStatus
        error match {
          case blocked: SyncCascadeBlocked =>
            (contentAsJson(result) \ "atomIdsPreventingSync")
              .as[List[String]] shouldBe blocked.atomIdsPreventingSync
          case _ =>
        }
      }
    }
  }

  "DynamoIconikAtomStore" should "decode union and legacy atoms and guard preview and published repairs by revision" in {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val client = Mockito.mock(classOf[DynamoDbAsyncClient])
    def str(v: String): AttributeValue = AttributeValue.builder().s(v).build()
    def number(v: Long): AttributeValue =
      AttributeValue.builder().n(v.toString).build()
    def attrMap(entries: (String, AttributeValue)*): AttributeValue =
      AttributeValue.builder().m(entries.toMap.asJava).build()

    val unionItem = Map(
      "id" -> str("atom-union"),
      "contentChangeDetails" -> attrMap("revision" -> number(7)),
      "data" -> attrMap(
        "media" -> attrMap(
          "metadata" -> attrMap(
            "iconik" -> attrMap(
              "projectId" -> str("proj-1"),
              "commissionId" -> str("old-comm"),
              "workingGroupId" -> str("wg-1")
            )
          )
        )
      )
    ).asJava
    val legacyItem = Map(
      "id" -> str("atom-legacy"),
      "contentChangeDetails" -> attrMap("revision" -> number(9)),
      "data" -> attrMap(
        "metadata" -> attrMap(
          "iconik" -> attrMap(
            "projectId" -> str("proj-1"),
            "commissionId" -> str("old-comm"),
            "workingGroupId" -> str("wg-1")
          )
        )
      )
    ).asJava
    val publishedItem = (unionItem.asScala.toMap ++ Map(
      "id" -> str("atom-published"),
      "contentChangeDetails" -> attrMap("revision" -> number(11))
    )).asJava

    Mockito
      .when(client.scan(org.mockito.ArgumentMatchers.any(classOf[ScanRequest])))
      .thenAnswer { invocation =>
        val req = invocation.getArgument[ScanRequest](0)
        val items =
          if (req.tableName() == "preview") List(unionItem, legacyItem).asJava
          else List(publishedItem).asJava
        CompletableFuture.completedFuture(
          ScanResponse.builder().items(items).build()
        )
      }

    val atomStore = new DynamoIconikAtomStore(
      client,
      previewTableName = "preview",
      publishedTableName = "published",
      scanSegments = 1
    )

    val scanned = Await.result(atomStore.listLinkedAtoms(), 5.seconds)
    scanned shouldBe Right(
      List(
        AtomIconikRef(
          "atom-union",
          isPublished = false,
          isLegacyEncoding = false,
          Some("wg-1"),
          Some("old-comm"),
          Some("proj-1"),
          revision = 7
        ),
        AtomIconikRef(
          "atom-legacy",
          isPublished = false,
          isLegacyEncoding = true,
          Some("wg-1"),
          Some("old-comm"),
          Some("proj-1"),
          revision = 9
        ),
        AtomIconikRef(
          "atom-published",
          isPublished = true,
          isLegacyEncoding = false,
          Some("wg-1"),
          Some("old-comm"),
          Some("proj-1"),
          revision = 11
        )
      )
    )

    Mockito
      .when(
        client.updateItem(
          org.mockito.ArgumentMatchers.any(classOf[UpdateItemRequest])
        )
      )
      .thenReturn(
        CompletableFuture.completedFuture(UpdateItemResponse.builder().build()),
        CompletableFuture.failedFuture[UpdateItemResponse](
          ConditionalCheckFailedException.builder().message("changed").build()
        ),
        CompletableFuture.completedFuture(UpdateItemResponse.builder().build())
      )

    val refs = scanned.toOption.get
    Await.result(
      atomStore.updateProjectParents(refs.head, "proj-1", "new-comm", "wg-1"),
      5.seconds
    ) shouldBe Right(true)
    Await.result(
      atomStore.updateProjectParents(refs(1), "proj-1", "new-comm", "wg-1"),
      5.seconds
    ) shouldBe Right(false)
    Await.result(
      atomStore.updateProjectParents(refs(2), "proj-1", "new-comm", "wg-1"),
      5.seconds
    ) shouldBe Right(true)

    val captor = ArgumentCaptor.forClass(classOf[UpdateItemRequest])
    Mockito.verify(client, Mockito.times(3)).updateItem(captor.capture())
    val requests = captor.getAllValues.asScala.toList
    requests.head.updateExpression() should include("#data.#media.#metadata")
    requests(1).updateExpression() should include("#data.#metadata.#iconik")
    requests.map(_.tableName()) shouldBe List("preview", "preview", "published")
    requests.zip(List(7L, 9L, 11L)).foreach { case (request, revision) =>
      request.conditionExpression() should include(
        "#changes.#revision = :revision"
      )
      request.updateExpression() should include(
        "#changes.#revision = :newRevision"
      )
      request
        .expressionAttributeValues()
        .get(":revision")
        .n() shouldBe revision.toString
      request
        .expressionAttributeValues()
        .get(":newRevision")
        .n() shouldBe (revision + 1).toString
      request
        .expressionAttributeNames()
        .get("#changes") shouldBe "contentChangeDetails"
    }

    Mockito
      .when(client.scan(any(classOf[ScanRequest])))
      .thenReturn(
        CompletableFuture.completedFuture(
          ScanResponse
            .builder()
            .items((unionItem.asScala.toMap - "contentChangeDetails").asJava)
            .build()
        )
      )
    Await.result(atomStore.listLinkedAtoms(), 5.seconds).isLeft shouldBe true
  }
}
