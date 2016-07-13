package test

import com.google.inject.AbstractModule
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

import org.scalatestplus.play.{ PlaySpec, OneAppPerSuite }

import com.gu.pandomainauth.action.AuthActions

import data._

import controllers.Api

import play.api.inject.bind

trait MediaAtomSuite extends PlaySpec with OneAppPerSuite {

  val guicer = new GuiceApplicationBuilder()
    .overrides(bind(classOf[AuthActions]).to(classOf[TestPandaAuth]))

  override lazy val app = guicer.build

  val oneHour = 3600000L
  def getApi(dataStore: DataStore, livePublisher: LiveAtomPublisher, previewPublisher: PreviewAtomPublisher) = {
    guicer
      .overrides(bind(classOf[DataStore]).toInstance(dataStore))
      .overrides(bind(classOf[LiveAtomPublisher]).toInstance(livePublisher))
      .overrides(bind(classOf[PreviewAtomPublisher]).toInstance(previewPublisher))
      .injector
      .instanceOf(classOf[Api])
  }
}
