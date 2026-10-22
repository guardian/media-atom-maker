package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.iconik.{
  CachedEntitiesFailure,
  IconikRequestFailure,
  IconikValidationReport,
  IconikValidationService,
  IconikValidationSummary
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
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, verifyZeroInteractions, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import java.security.KeyPairGenerator
import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class IconikValidationControllerTest extends AnyFlatSpec with Matchers {
  private val controllerComponents = Helpers.stubControllerComponents()
  private val permissions = mock[MediaAtomMakerPermissionsProvider]
  private val settings = mock[PanDomainAuthSettingsRefresher]
  private val authWsClient = mock[WSClient]
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
      IconikValidationControllerTest.this.controllerComponents
    override val panDomainSettings: PanDomainAuthSettingsRefresher = settings
    override def permissionsProvider: MediaAtomMakerPermissionsProvider =
      IconikValidationControllerTest.this.permissions
  }

  private def controller(
      service: IconikValidationService
  ): IconikValidationController =
    new IconikValidationController(
      authActions,
      service,
      controllerComponents
    )

  private def callValidate(
      controller: IconikValidationController,
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
      FakeRequest(GET, "/api/iconik/validate").withCookies(cookie)

    controller.validateEntities.apply(request)
  }

  "validateEntities" should "return the validation report" in {
    val report = IconikValidationReport(
      entities = Nil,
      summary = IconikValidationSummary(0, 0, 0, 0)
    )
    val service = mock[IconikValidationService]
    when(service.validate()).thenReturn(Future.successful(Right(report)))

    val result = callValidate(controller(service))

    status(result) shouldBe OK
    contentAsJson(result) shouldBe report.toJson
  }

  it should "reject an invalid browser user without running validation" in {
    val service = mock[IconikValidationService]

    status(callValidate(controller(service), validUser = false)) shouldBe
      FORBIDDEN
    verifyZeroInteractions(service)
  }

  it should "map cached-data and Iconik failures to their HTTP statuses" in {
    val failures = Seq(
      CachedEntitiesFailure("cache unavailable") ->
        (SERVICE_UNAVAILABLE, "Failed to read cached Iconik entities"),
      IconikRequestFailure("Iconik unavailable") ->
        (BAD_GATEWAY, "Failed to validate entities with Iconik")
    )

    failures.foreach { case (failure, (expectedStatus, expectedMessage)) =>
      val service = mock[IconikValidationService]
      when(service.validate())
        .thenReturn(Future.successful(Left(failure)))

      val result = callValidate(controller(service))

      status(result) shouldBe expectedStatus
      (contentAsJson(result) \ "error").as[String] shouldBe expectedMessage
    }
  }
}
