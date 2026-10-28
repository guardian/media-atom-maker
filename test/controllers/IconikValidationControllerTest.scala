package controllers

import com.gu.media.MediaAtomMakerPermissionsProvider
import com.gu.media.iconik.{
  DynamoFailure,
  IconikEntityId,
  IconikEntityType,
  IconikRequestFailure,
  IconikValidationReport,
  IconikValidationService,
  IconikValidationSummary
}
import com.gu.pandomainauth.PanDomainAuthSettingsRefresher
import org.mockito.MockitoSugar.{mock, verifyZeroInteractions, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._

import java.nio.charset.StandardCharsets.UTF_8
import java.time.{Clock, Instant, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.{Base64, Locale}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.concurrent.Future

class IconikValidationControllerTest extends AnyFlatSpec with Matchers {
  private val controllerComponents = Helpers.stubControllerComponents()
  private val permissions = mock[MediaAtomMakerPermissionsProvider]
  private val settings = mock[PanDomainAuthSettingsRefresher]
  private val authWsClient = mock[WSClient]

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
      entityType: String = "project",
      id: String = "project-1"
  ) = {
    val path = s"/api/iconik/validate/$entityType/$id"
    val request: FakeRequest[AnyContent] =
      FakeRequest(GET, path).withHeaders(hmacHeaders(path): _*)

    controller.validateEntity(entityType, id).apply(request)
  }

  private def hmacHeaders(path: String): Seq[(String, String)] = {
    val date = DateTimeFormatter
      .ofPattern("EEE, dd MMM uuuu HH:mm:ss 'GMT'", Locale.US)
      .withZone(ZoneOffset.UTC)
      .format(Instant.now())
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec("test-secret".getBytes(UTF_8), "HmacSHA256"))
    val signature =
      Base64.getEncoder.encodeToString(
        mac.doFinal(s"$date\n$path".getBytes(UTF_8))
      )
    Seq(
      "X-Gu-Tools-HMAC-Date" -> date,
      "X-Gu-Tools-HMAC-Token" -> s"HMAC $signature",
      "X-Gu-Tools-Service-Name" -> "test-iconik-checker"
    )
  }

  "validateEntity" should "return the validation report" in {
    val report = IconikValidationReport(
      entities = Nil,
      summary = IconikValidationSummary(0, 0, 0, 0)
    )
    val service = mock[IconikValidationService]
    when(
      service.validate(IconikEntityType.Project, "project-1")
    ).thenReturn(Future.successful(Right(report)))

    val result = callValidate(controller(service))

    status(result) shouldBe OK
    contentAsJson(result) shouldBe report.toJson
  }

  it should "allow a signed shell request" in {
    val path = "/api/iconik/validate/project/project-1"
    val report = IconikValidationReport(
      entities = Nil,
      summary = IconikValidationSummary(0, 0, 0, 0)
    )
    val service = mock[IconikValidationService]
    when(
      service.validate(IconikEntityType.Project, "project-1")
    ).thenReturn(Future.successful(Right(report)))
    val request = FakeRequest(GET, path).withHeaders(hmacHeaders(path): _*)

    status(
      controller(service).validateEntity("project", "project-1").apply(request)
    ) shouldBe
      OK
  }

  it should "list entity IDs for the shell checker" in {
    val service = mock[IconikValidationService]
    when(service.listEntityIds()).thenReturn(
      Future.successful(
        Right(List(IconikEntityId("project", "project-1")))
      )
    )
    val path = "/api/iconik/validate-entities"
    val request = FakeRequest(GET, path).withHeaders(hmacHeaders(path): _*)

    val result = controller(service).listEntities.apply(request)

    status(result) shouldBe OK
    contentAsJson(result) shouldBe Json.arr(
      Json.obj("entityType" -> "project", "id" -> "project-1")
    )
  }

  it should "reject unsupported entity types without running validation" in {
    val service = mock[IconikValidationService]

    status(callValidate(controller(service), entityType = "asset")) shouldBe
      BAD_REQUEST
    verifyZeroInteractions(service)
  }

  it should "map Dynamo and Iconik failures to their HTTP statuses" in {
    val failures = Seq(
      DynamoFailure("dynamo unavailable") ->
        (SERVICE_UNAVAILABLE, "Failed to read Iconik entity from Dynamo"),
      IconikRequestFailure("Iconik unavailable") ->
        (BAD_GATEWAY, "Failed to validate entity with Iconik")
    )

    failures.foreach { case (failure, (expectedStatus, expectedMessage)) =>
      val service = mock[IconikValidationService]
      when(
        service.validate(IconikEntityType.Project, "project-1")
      )
        .thenReturn(Future.successful(Left(failure)))

      val result = callValidate(controller(service))

      status(result) shouldBe expectedStatus
      (contentAsJson(result) \ "error").as[String] shouldBe expectedMessage
    }
  }
}
