package com.gu.atom.play.test

import scala.collection.mutable.{ Map => MMap }

import com.google.inject.AbstractModule
import javax.inject.Provider
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import com.gu.atom.publish.AtomPublisher
import java.util.Date
import play.api.inject.guice.{ GuiceApplicationBuilder, GuiceableModule, GuiceableModuleConversions }

import org.scalatestplus.play.PlaySpec

import com.gu.atom.data._

//import controllers.Api

import play.api.inject.{ bind, Binding }
//import play.api.test.FakeRequest
import scala.reflect.ClassTag

import org.scalatest.mock.MockitoSugar.mock

trait AtomSuite extends PlaySpec with GuiceableModuleConversions {

  def initialDataStore = mock[DataStore]
  def initialLivePublisher = mock[LiveAtomPublisher]
  def initialPreviewPublisher = mock[PreviewAtomPublisher]

  def customOverrides: Seq[Binding[_]] = Seq.empty

  protected def ibind[A : ClassTag](a: A): Binding[A] = bind[A] toInstance a
  // bind to a simple mock
  protected def mbind[A <: AnyRef : ClassTag : Manifest] = ibind[A](mock[A])

  case class AtomTestConf(
    dataStore: DataStore = initialDataStore,
    livePublisher: LiveAtomPublisher = initialLivePublisher,
    previewPublisher: PreviewAtomPublisher = initialPreviewPublisher,
    shutDownHook: AtomTestConf => Unit = _.app.stop) {

    private def makeOverrides: GuiceableModule = Seq(
      ibind(dataStore),
      ibind(livePublisher),
      ibind(previewPublisher)
    ) ++ customOverrides

    lazy val guicer = new GuiceApplicationBuilder()
//      .overrides()
      .overrides(makeOverrides)

    lazy val app = guicer.build()

    def iget[A](implicit c: ClassTag[A]): A = app.injector.instanceOf[A]

//    lazy val api = app.injector.instanceOf(classOf[Api])

    def shutdown = shutDownHook(this)

    def apply(block: AtomTestConf => Unit) =
      try {
        block(this)
      } finally {
        shutdown
      }
  }

  implicit def app(implicit atomConf: AtomTestConf) = atomConf.app
  implicit def materializer(implicit atomConf: AtomTestConf) = app.materializer
}
