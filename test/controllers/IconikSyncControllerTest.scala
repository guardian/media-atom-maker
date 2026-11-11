package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.iconik.{
  AtomIconikRef,
  DynamoFailure,
  DynamoIconikAtomStore,
  IconikRequestFailure,
  IconikSyncResult,
  IconikSyncService,
  SyncCascadeBlocked,
  SyncDynamoError,
  SyncIconikError,
  SyncInvalidHierarchy,
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
      service: IconikSyncService[DynamoReadError]
  ): IconikController =
    new IconikController(
      ConfigFactory.empty(),
      awsConfig,
      authActions,
      stores,
      service,
      controllerComponents
    )

  private def callSync(
      controller: IconikController,
      id: String = "proj-1",
      validUser: Boolean = true
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
      FakeRequest(POST, s"/api/iconik/collections/$id/sync")
        .withCookies(cookie)

    controller.syncCollection(id).apply(request)
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
  }

  it should "reject unauthorized callers before invoking the sync service" in {
    val service = mock[IconikSyncService[DynamoReadError]]

    status(callSync(controller(service), validUser = false)) shouldBe FORBIDDEN
    verifyZeroInteractions(service)
  }

  it should "map domain and infrastructure errors to their HTTP statuses" in {
    val cases = Seq(
      SyncNotFound("missing") -> NOT_FOUND,
      SyncCascadeBlocked(
        "comm-1",
        "commission",
        "sync child projects first",
        blockingChildIds = List("proj-1")
      ) -> CONFLICT,
      SyncInvalidHierarchy("bad", "invalid hierarchy") -> UNPROCESSABLE_ENTITY,
      SyncIconikError(IconikRequestFailure("iconik 500")) -> BAD_GATEWAY,
      SyncDynamoError(DynamoFailure("dynamo down")) -> SERVICE_UNAVAILABLE
    )

    cases.foreach { case (error, expectedStatus) =>
      val service = mock[IconikSyncService[DynamoReadError]]
      when(service.sync("proj-1")).thenReturn(Future.successful(Left(error)))

      val result = callSync(controller(service))

      status(result) shouldBe expectedStatus
    }
  }

  "DynamoIconikAtomStore" should "decode union and legacy media atoms and tolerate conditional check races" in {
    implicit val ec: ExecutionContext = ExecutionContext.global
    val client = Mockito.mock(classOf[DynamoDbAsyncClient])
    def str(v: String): AttributeValue = AttributeValue.builder().s(v).build()
    def attrMap(entries: (String, AttributeValue)*): AttributeValue =
      AttributeValue.builder().m(entries.toMap.asJava).build()

    val unionItem = Map(
      "id" -> str("atom-union"),
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

    Mockito
      .when(client.scan(org.mockito.ArgumentMatchers.any(classOf[ScanRequest])))
      .thenAnswer { invocation =>
        val req = invocation.getArgument[ScanRequest](0)
        val items =
          if (req.tableName() == "preview") List(unionItem, legacyItem).asJava
          else List.empty[java.util.Map[String, AttributeValue]].asJava
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
          Some("proj-1")
        ),
        AtomIconikRef(
          "atom-legacy",
          isPublished = false,
          isLegacyEncoding = true,
          Some("wg-1"),
          Some("old-comm"),
          Some("proj-1")
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
        )
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

    val captor = ArgumentCaptor.forClass(classOf[UpdateItemRequest])
    Mockito.verify(client, Mockito.times(2)).updateItem(captor.capture())
    val requests = captor.getAllValues.asScala.toList
    requests.head.updateExpression() should include("#data.#media.#metadata")
    requests(1).updateExpression() should include("#data.#metadata.#iconik")
  }
}
