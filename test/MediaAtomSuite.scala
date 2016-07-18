package test

import com.google.inject.AbstractModule
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import com.gu.atom.publish.AtomPublisher
import com.gu.pandomainauth.model.{ AuthenticatedUser, User }
import java.util.Date
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceableModule, GuiceableModuleConversions }

import org.scalatestplus.play.PlaySpec

import com.gu.pandomainauth.action.AuthActions

import com.gu.atom.data._

import controllers.Api

import play.api.inject.{ bind, Binding }
import play.api.test.FakeRequest
import scala.reflect.ClassTag

import org.scalatest.mock.MockitoSugar.mock

trait MediaAtomSuite extends PlaySpec with GuiceableModuleConversions {

  def testUser: AuthenticatedUser = AuthenticatedUser(
    user = User("Homer", "Simpson", "homer.simpson@guardian.co.uk", None),
    authenticatingSystem = "test",
    authenticatedIn = Set("test"),
    expires = new Date().getTime + oneHour,
    multiFactor = true
  )

  def initialDataStore = mock[DataStore]
  def initialLivePublisher = mock[LiveAtomPublisher]
  def initialPreviewPublisher = mock[PreviewAtomPublisher]

  private def ibind[A : ClassTag](a: A): Binding[A] = bind[A] toInstance a

  case class AtomTestConf(
    dataStore: DataStore = initialDataStore,
    livePublisher: LiveAtomPublisher = initialLivePublisher,
    previewPublisher: PreviewAtomPublisher = initialPreviewPublisher,
    shutDownHook: AtomTestConf => Unit = _.app.stop) {

    private def makeOverrides: GuiceableModule = Seq(
      ibind(dataStore),
      ibind(livePublisher),
      ibind(previewPublisher)
    )

    lazy val guicer = new GuiceApplicationBuilder()
      .overrides(bind(classOf[AuthActions]).to(classOf[TestPandaAuth]))
      .overrides(makeOverrides)

    lazy val app = guicer.build()
    lazy val api = app.injector.instanceOf(classOf[Api])

    def shutdown = shutDownHook(this)

    def apply(block: AtomTestConf => Unit) =
      try {
        block(this)
      } finally {
        shutdown
      }
  }

  def requestWithCookies(implicit conf: AtomTestConf) =
    FakeRequest().withCookies(api.authActions.generateCookies(testUser): _*)

  implicit def app(implicit atomConf: AtomTestConf) = atomConf.app
  implicit def materializer(implicit atomConf: AtomTestConf) = app.materializer

  def api(implicit atomConf: AtomTestConf) = atomConf.api

  val oneHour = 3600000L
}
