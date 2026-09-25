package controllers

import com.gu.atom.data.{PreviewDynamoDataStoreV2, PublishedDynamoDataStoreV2}
import com.gu.atom.play.ReindexController
import com.gu.atom.publish.AtomPublisher
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.hmac.HMACDate.DateTimeOps
import com.gu.media.{Capi, MediaAtomMakerPermissionsProvider, Permissions}
import com.gu.media.model.{MediaAtom, User => AtomUser}
import com.gu.media.telemetry.Telemetry
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
import org.joda.time.DateTime
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, verify, verifyZeroInteractions, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.{
  ApplicationLoader,
  BuiltInComponentsFromContext,
  Configuration,
  Environment,
  Mode
}
import play.api.inject.DefaultApplicationLifecycle
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{
  ControllerComponents,
  DefaultCookieHeaderEncoding,
  DefaultSessionCookieBaker,
  EssentialFilter,
  Headers,
  Session
}
import play.api.test.{FakeRequest, FakeRequestFactory}
import play.api.test.Helpers._
import play.filters.HttpFiltersComponents
import router.Routes
import schedule.GridAPI
import util.{AWSConfig, ThumbnailGenerator, YouTube}

import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyPairGenerator
import java.time.Instant
import java.util.{Base64, UUID}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import scala.util.Success

class ApiCreationTest extends AnyFlatSpec with Matchers {
  private val secret = "synthetic-mam-hmac-secret"
  private val serviceName = "test-draft-creator"
  private val browserUser =
    User("Test", "Editor", "test.editor@guardian.co.uk", None)
  private val payload = Json.obj(
    "title" -> "Test draft",
    "category" -> "News",
    "contentChangeDetails" -> Json.obj("revision" -> 0),
    "keywords" -> Json.arr(),
    "blockAds" -> false,
    "tags" -> Json.arr(),
    "byline" -> Json.arr(),
    "commissioningDesks" -> Json.arr(),
    "atomTagIds" -> Json.arr()
  )

  private val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
  private val signing = CryptoConf.SigningAndVerification(
    KeyPair(keyPair.getPublic, keyPair.getPrivate),
    Nil
  )

  private def request =
    FakeRequest(POST, "/api/atoms").withBody[JsValue](payload)

  private def hmacHeaders(
      signingSecret: String = secret
  ): Seq[(String, String)] = {
    val date = DateTime.now().toRfc7231String
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(signingSecret.getBytes(UTF_8), "HmacSHA256"))
    val signature =
      Base64.getEncoder.encodeToString(
        mac.doFinal(s"$date\n/api/atoms".getBytes(UTF_8))
      )
    Seq(
      "X-Gu-Tools-HMAC-Date" -> date,
      "X-Gu-Tools-HMAC-Token" -> s"HMAC $signature",
      "X-Gu-Tools-Service-Name" -> serviceName
    )
  }

  private class TestComponents
      extends BuiltInComponentsFromContext(
        ApplicationLoader.Context(
          environment = Environment.simple(mode = Mode.Test),
          // Do not load application.conf: it includes private developer configuration.
          initialConfiguration = Configuration(
            ConfigFactory
              .parseString("""
                play.http.secret.key = "synthetic-play-session-secret-for-creation-tests"
                secret = "synthetic-mam-hmac-secret"
                host = "mam.example.test"
              """)
              .withFallback(ConfigFactory.defaultReference())
              .resolve()
          ),
          lifecycle = new DefaultApplicationLifecycle(),
          devContext = None
        )
      )
      with HttpFiltersComponents {
    // Match MediaAtomMaker's filter stack without constructing its AWS/OAuth clients.
    override lazy val httpFilters: Seq[EssentialFilter] =
      super.httpFilters.filterNot(_ == allowedHostsFilter)

    val preview = mock[PreviewDynamoDataStoreV2]
    val published = mock[PublishedDynamoDataStoreV2]
    val previewPublisher = mock[AtomPublisher]
    val livePublisher = mock[AtomPublisher]
    val stores = mock[DataStores]
    val permissions = mock[MediaAtomMakerPermissionsProvider]
    val youtube = mock[YouTube]
    val aws = mock[AWSConfig]
    val capi = mock[Capi]
    val thumbnails = mock[ThumbnailGenerator]
    val grid = mock[GridAPI]
    val telemetry = mock[Telemetry]
    val ws = mock[WSClient]
    val uploads = mock[UploadController]
    val settings = mock[PanDomainAuthSettingsRefresher]

    when(stores.preview).thenReturn(preview)
    when(stores.published).thenReturn(published)
    when(stores.previewPublisher).thenReturn(previewPublisher)
    when(stores.livePublisher).thenReturn(livePublisher)
    when(preview.createAtom(any[Atom])).thenAnswer((atom: Atom) => Right(atom))
    when(previewPublisher.publishAtomEvent(any[ContentAtomEvent]))
      .thenReturn(Success(()))
    when(permissions.hasPermission(Permissions.basicAccess, browserUser))
      .thenReturn(true)
    when(settings.system).thenReturn("video")
    when(settings.domain).thenReturn("example.test")
    when(settings.settings).thenReturn(
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

    val auth = new PanDomainAuthActions {
      override def conf: Configuration = TestComponents.this.configuration
      override def wsClient: WSClient = ws
      override def controllerComponents: ControllerComponents =
        TestComponents.this.controllerComponents
      override val panDomainSettings: PanDomainAuthSettingsRefresher = settings
      override val permissionsProvider: MediaAtomMakerPermissionsProvider =
        permissions
    }

    private val api = new Api(
      stores,
      configuration,
      auth,
      youtube,
      aws,
      permissions,
      capi,
      thumbnails,
      grid,
      telemetry,
      controllerComponents
    )

    override lazy val router = new Routes(
      httpErrorHandler,
      api,
      mock[PlutoController],
      mock[IconikController],
      uploads,
      mock[Youtube],
      mock[ReindexController],
      mock[VideoUIApp],
      mock[Support],
      mock[Login],
      mock[Healthcheck],
      mock[Assets]
    )

    def browserRequest(
        user: User = browserUser,
        multiFactor: Boolean = true,
        withCsrf: Boolean = true
    ): FakeRequest[JsValue] = {
      val authedUser = AuthenticatedUser(
        user,
        "video",
        Set("video"),
        Instant.now().plusSeconds(3600),
        multiFactor
      )
      val cookie = auth.generateCookie(authedUser)
      val cookieEncoding = new DefaultCookieHeaderEncoding()
      val headers = if (withCsrf) {
        val token = csrfTokenProvider.generateToken
        val sessionCookieBaker = new DefaultSessionCookieBaker(
          httpConfiguration.session,
          httpConfiguration.secret,
          cookieSigner
        )
        val sessionCookie =
          sessionCookieBaker.encodeAsCookie(Session(Map("csrfToken" -> token)))
        Seq(
          COOKIE -> cookieEncoding.encodeCookieHeader(
            Seq(cookie, sessionCookie)
          ),
          "Csrf-Token" -> token
        )
      } else Seq(COOKIE -> cookieEncoding.encodeCookieHeader(Seq(cookie)))
      new FakeRequestFactory(requestFactory).apply(
        POST,
        "/api/atoms",
        Headers(headers: _*),
        payload
      )
    }

    def assertNoExternalEffects(): Unit =
      verifyZeroInteractions(
        published,
        livePublisher,
        youtube,
        aws,
        capi,
        thumbnails,
        grid,
        telemetry,
        ws,
        uploads
      )

    def assertRejected(req: FakeRequest[JsValue], expectedStatus: Int): Unit = {
      status(route(application, req).get) shouldBe expectedStatus
      verifyZeroInteractions(preview, previewPublisher)
      assertNoExternalEffects()
    }

    def assertCreated(
        req: FakeRequest[JsValue],
        expectedUser: AtomUser
    ): Unit = {
      val result = route(application, req).get
      status(result) shouldBe CREATED
      val json = contentAsJson(result)
      val id = (json \ "id").as[String]
      UUID.fromString(id).toString shouldBe id
      header(LOCATION, result) shouldBe Some(s"/atom/$id")

      val saved = ArgumentCaptor.forClass(classOf[Atom])
      val event = ArgumentCaptor.forClass(classOf[ContentAtomEvent])
      verify(preview).createAtom(saved.capture())
      verify(previewPublisher).publishAtomEvent(event.capture())
      saved.getValue.id shouldBe id
      event.getValue.atom shouldBe saved.getValue
      event.getValue.eventType shouldBe EventType.Update

      val atom = json.as[MediaAtom]
      atom shouldBe MediaAtom.fromThrift(saved.getValue)
      atom.title shouldBe "Test draft"
      atom.contentChangeDetails.revision shouldBe 1L
      atom.contentChangeDetails.published shouldBe None
      atom.contentChangeDetails.scheduledLaunch shouldBe None
      atom.contentChangeDetails.lastModified shouldBe atom.contentChangeDetails.created
      atom.contentChangeDetails.created.flatMap(_.user) shouldBe Some(
        expectedUser
      )
      atom.assets shouldBe Nil
      atom.activeVersion shouldBe None
      atom.duration shouldBe None
      assertNoExternalEffects()
    }
  }

  private def withApp(test: TestComponents => Unit): Unit = {
    val components = new TestComponents
    running(components.application) {
      test(components)
    }
  }

  "POST /api/atoms" should "create a metadata-only preview draft using HMAC without a browser session" in withApp {
    app =>
      app.assertCreated(
        request.withHeaders(hmacHeaders(): _*),
        AtomUser(serviceName, None, None)
      )
      verifyZeroInteractions(app.permissions)
  }

  it should "reject an invalid HMAC without creating an atom" in withApp {
    app =>
      app.assertRejected(
        request.withHeaders(hmacHeaders("wrong-secret"): _*),
        UNAUTHORIZED
      )
  }

  it should "reject requests without HMAC or a browser session" in withApp {
    app =>
      app.assertRejected(request, UNAUTHORIZED)
  }

  it should "retain browser authentication and CSRF support without HMAC headers" in withApp {
    app =>
      app.assertCreated(
        app.browserRequest(),
        AtomUser(browserUser.email, Some("Test"), Some("Editor"))
      )
      verify(app.permissions)
        .hasPermission(Permissions.basicAccess, browserUser)
  }

  it should "reject an invalid HMAC even when a valid browser session is present" in withApp {
    app =>
      app.assertRejected(
        app.browserRequest().withHeaders(hmacHeaders("wrong-secret"): _*),
        UNAUTHORIZED
      )
  }

  it should "reject a missing required field after valid HMAC authentication" in withApp {
    app =>
      app.assertRejected(
        request
          .withBody[JsValue](payload - "title")
          .withHeaders(hmacHeaders(): _*),
        BAD_REQUEST
      )
  }

  it should "retain CSRF protection for browser sessions" in withApp { app =>
    app.assertRejected(app.browserRequest(withCsrf = false), FORBIDDEN)
  }

  it should "reject a browser user without basic access" in withApp { app =>
    when(app.permissions.hasPermission(Permissions.basicAccess, browserUser))
      .thenReturn(false)
    app.assertRejected(app.browserRequest(), FORBIDDEN)
  }

  it should "reject a browser user without MFA" in withApp { app =>
    app.assertRejected(app.browserRequest(multiFactor = false), FORBIDDEN)
  }

  it should "reject a browser user outside the Guardian domain even with basic access" in withApp {
    app =>
      val user = browserUser.copy(email = "editor@example.test")
      when(app.permissions.hasPermission(Permissions.basicAccess, user))
        .thenReturn(true)
      app.assertRejected(app.browserRequest(user = user), FORBIDDEN)
  }
}
