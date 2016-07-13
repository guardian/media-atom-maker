package test

import com.google.inject.AbstractModule
import com.gu.atom.publish.AtomPublisher
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
  def getApi(dataStore: DataStore, publisher: AtomPublisher) = {
    guicer
      .overrides(bind(classOf[DataStore]).toInstance(dataStore))
      .overrides(bind(classOf[AtomPublisher]).toInstance(publisher))
      .injector
      .instanceOf(classOf[Api])
  }
}
