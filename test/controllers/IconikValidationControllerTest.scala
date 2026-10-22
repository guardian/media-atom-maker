package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.iconik.{IconikCommission, IconikProject, IconikWorkingGroup}
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import com.gu.pandomainauth.model.{
  AuthenticatedUser,
  CookieSettings,
  OAuthSettings,
  PanDomainAuthSettings,
  User
}
import com.gu.pandomainauth.service.{CryptoConf, KeyPair}
import data.{DataStores, IconikStore}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import org.scanamo.{DynamoReadError, MissingProperty}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import play.api.libs.json.{JsArray, JsBoolean, JsNull, JsObject, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import util.AWSConfig

import java.time.{Clock, Instant, ZoneOffset}
import java.security.KeyPairGenerator
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class IconikValidationControllerTest extends AnyFlatSpec with Matchers {
  private val testControllerComponents = Helpers.stubControllerComponents()
  private val hmacSecret = "test-hmac-secret"
  private val apiPath = "/api/iconik/validate"
  private val iconikAppId = "test-iconik-app"
  private val iconikToken = "test-iconik-token"
  private val credentialsArn = "test-iconik-credentials-arn"
  private val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
  private val signing = CryptoConf.SigningAndVerification(
    KeyPair(keyPair.getPublic, keyPair.getPrivate),
    Nil
  )
  private val authSettings = mock[PanDomainAuthSettingsRefresher]
  private val permissionsProvider = mock[MediaAtomMakerPermissionsProvider]
  private val authWsClient = mock[WSClient]
  private val browserUser =
    User("Test", "Editor", "test.editor@guardian.co.uk", None)

  when(authSettings.system).thenReturn("video")
  when(authSettings.domain).thenReturn("example.test")
  when(authSettings.settings).thenReturn(
    PanDomainAuthSettings(
      signing,
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
  when(permissionsProvider.hasPermission(any(), any())).thenReturn(true)

  private val authActions = new PanDomainAuthActions {
    override protected val clock: Clock =
      Clock.fixed(Instant.now(), ZoneOffset.UTC)
    override def conf: Configuration = Configuration(
      "secret" -> hmacSecret,
      "host" -> "example.test"
    )
    override def wsClient: WSClient = authWsClient
    override def controllerComponents: ControllerComponents =
      testControllerComponents
    override val panDomainSettings: PanDomainAuthSettingsRefresher =
      authSettings
    override def permissionsProvider: MediaAtomMakerPermissionsProvider =
      IconikValidationControllerTest.this.permissionsProvider
  }

  private def storesWith(
      projects: Either[DynamoReadError, List[IconikProject]] = Right(Nil),
      commissions: Either[DynamoReadError, List[IconikCommission]] = Right(Nil),
      workingGroups: Either[DynamoReadError, List[IconikWorkingGroup]] =
        Right(Nil)
  ): DataStores = {
    val stores = mock[DataStores]
    val iconikStore = mock[IconikStore[DynamoReadError]]
    when(stores.iconikDataStore).thenReturn(iconikStore)
    when(iconikStore.listProjectsAsync()(any[ExecutionContext]))
      .thenReturn(Future.successful(projects))
    when(iconikStore.listCommissionsAsync()(any[ExecutionContext]))
      .thenReturn(Future.successful(commissions))
    when(iconikStore.listWorkingGroupsAsync()(any[ExecutionContext]))
      .thenReturn(Future.successful(workingGroups))
    stores
  }

  private def controller(stores: DataStores, wsClient: WSClient) = {
    val awsConfig = mock[AWSConfig]
    when(awsConfig.getString("aws.secretsmanager.iconikCredentials"))
      .thenReturn(Some(credentialsArn))
    when(awsConfig.getSecret(credentialsArn))
      .thenReturn(
        Success(
          Json
            .obj("app-id" -> iconikAppId, "token" -> iconikToken)
            .toString
        )
      )

    new IconikValidationController(
      authActions,
      stores,
      awsConfig,
      wsClient,
      testControllerComponents
    )
  }

  private def callValidate(
      controller: IconikValidationController,
      validUser: Boolean = true
  ) = {
    val user =
      if (validUser) browserUser
      else browserUser.copy(email = "test.editor@example.test")
    val authenticatedUser = AuthenticatedUser(
      user,
      "video",
      Set("video"),
      Instant.now().plusSeconds(3600),
      multiFactor = true
    )
    val cookie = authActions.generateCookie(authenticatedUser)
    val request: FakeRequest[AnyContent] =
      FakeRequest(GET, apiPath).withCookies(cookie)
    controller.validateEntities.apply(request)
  }

  private def stubIconikResponse(
      wsClient: WSClient,
      entityId: String,
      response: WSResponse
  ): Unit = {
    val request = mock[WSRequest]
    when(
      wsClient.url(
        s"https://app.iconik.io/API/assets/v1/collections/$entityId/"
      )
    ).thenReturn(request)
    when(
      request.addHttpHeaders(
        "App-ID" -> iconikAppId,
        "Auth-Token" -> iconikToken,
        "Accept" -> "application/json"
      )
    ).thenReturn(request)
    when(request.get()).thenReturn(Future.successful(response))
  }

  private def iconikResponse(
      statusCode: Int,
      body: play.api.libs.json.JsValue = Json.obj()
  ): WSResponse = {
    val response = mock[WSResponse]
    when(response.status).thenReturn(statusCode)
    when(response.json).thenReturn(body)
    response
  }

  "controller construction" should "fail when the Iconik credentials ARN is missing" in {
    val awsConfig = mock[AWSConfig]
    when(awsConfig.getString("aws.secretsmanager.iconikCredentials"))
      .thenReturn(None)

    intercept[IllegalStateException] {
      new IconikValidationController(
        mock[HMACAuthActions],
        mock[DataStores],
        awsConfig,
        mock[WSClient],
        Helpers.stubControllerComponents()
      )
    }
  }

  it should "fail when the Iconik credentials secret cannot be retrieved" in {
    val awsConfig = mock[AWSConfig]
    val retrievalError = new RuntimeException("secret lookup failed")
    when(awsConfig.getString("aws.secretsmanager.iconikCredentials"))
      .thenReturn(Some("secret-arn"))
    when(awsConfig.getSecret("secret-arn"))
      .thenReturn(Failure(retrievalError))

    val exception = intercept[IllegalStateException] {
      new IconikValidationController(
        mock[HMACAuthActions],
        mock[DataStores],
        awsConfig,
        mock[WSClient],
        Helpers.stubControllerComponents()
      )
    }

    exception.getCause shouldBe retrievalError
  }

  "validateEntities" should "reject a browser user outside the Guardian domain" in {
    val wsClient = mock[WSClient]
    val controller = this.controller(mock[DataStores], wsClient)

    status(callValidate(controller, validUser = false)) shouldBe FORBIDDEN
  }

  it should "report mixed Iconik results and summary counts" in {
    val project = IconikProject(
      id = "project-1",
      title = "Cached project",
      status = "active",
      workingGroupId = "working-group-1",
      commissionId = "commission-1",
      masterPlaceholderId = None,
      createdAt = None
    )
    val commission = IconikCommission(
      workingGroupId = "working-group-1",
      id = "commission-1",
      title = "Cached commission"
    )
    val workingGroup = IconikWorkingGroup(
      id = "working-group-1",
      title = "Cached working group"
    )
    val wsClient = mock[WSClient]
    stubIconikResponse(
      wsClient,
      project.id,
      iconikResponse(
        OK,
        Json.obj("title" -> "Renamed project", "parent_id" -> "other-commission")
      )
    )
    stubIconikResponse(wsClient, commission.id, iconikResponse(NOT_FOUND))
    stubIconikResponse(
      wsClient,
      workingGroup.id,
      iconikResponse(
        OK,
        Json.obj(
          "title" -> workingGroup.title,
          "parent_id" -> "iconik-parent"
        )
      )
    )
    val controller = this.controller(
      storesWith(
        projects = Right(List(project)),
        commissions = Right(List(commission)),
        workingGroups = Right(List(workingGroup))
      ),
      wsClient
    )

    val result = callValidate(controller)

    status(result) shouldBe OK
    val body = contentAsJson(result)
    (body \ "summary").as[JsObject] shouldBe Json.obj(
      "total" -> 3,
      "missing" -> 1,
      "nameMismatches" -> 1,
      "parentMismatches" -> 1
    )
    val entities = (body \ "entities").as[JsArray].value
    (entities(0) \ "nameMatches").as[Boolean] shouldBe false
    (entities(0) \ "parentMatches").as[Boolean] shouldBe false
    (entities(1) \ "exists").as[Boolean] shouldBe false
    (entities(2) \ "parentMatches").as[JsValue] shouldBe JsNull
  }

  it should "return 503 when cached entities cannot be read" in {
    val controller = this.controller(
      storesWith(projects = Left(MissingProperty)),
      mock[WSClient]
    )

    val result = callValidate(controller)

    status(result) shouldBe SERVICE_UNAVAILABLE
    (contentAsJson(result) \ "error").as[String] shouldBe
      "Failed to read cached Iconik entities"
  }

  it should "return 502 when Iconik returns an unexpected status" in {
    val project = IconikProject(
      id = "project-1",
      title = "Cached project",
      status = "active",
      workingGroupId = "working-group-1",
      commissionId = "commission-1",
      masterPlaceholderId = None,
      createdAt = None
    )
    val wsClient = mock[WSClient]
    stubIconikResponse(
      wsClient,
      project.id,
      iconikResponse(INTERNAL_SERVER_ERROR)
    )
    val controller = this.controller(
      storesWith(projects = Right(List(project))),
      wsClient
    )

    val result = callValidate(controller)

    status(result) shouldBe BAD_GATEWAY
    (contentAsJson(result) \ "error").as[String] shouldBe
      "Failed to validate entities with Iconik"
  }

  "parentMatchesJson" should "return unknown when the expected parent is unavailable" in {
    IconikValidationController.parentMatchesJson(
      None,
      Some("iconik-parent")
    ) shouldBe JsNull
  }

  "parentMatches" should "return whether the Iconik parent matches" in {
    IconikValidationController.parentMatches(
      "expected-parent",
      Some("expected-parent")
    ) shouldBe true

    IconikValidationController.parentMatches(
      "expected-parent",
      Some("different-parent")
    ) shouldBe false
  }
}
