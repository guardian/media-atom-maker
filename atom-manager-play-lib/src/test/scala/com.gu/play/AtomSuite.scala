package com.gu.atom.play.test

import scala.util.{Failure, Success}
import cats.data.Xor
import com.gu.atom.TestData

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

import play.api.inject.{ bind, Binding }
import scala.reflect.ClassTag

import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import org.scalatest.mock.MockitoSugar.mock

trait AtomSuite extends PlaySpec with GuiceableModuleConversions {

  def dataStore = mock[DataStore]

  def previewDataStoreMockWithTestData = {
    val m = mock[PreviewDataStore]
    when(m.getAtom(any())).thenReturn(Some(TestData.testAtoms.head))
    when(m.listAtoms).thenReturn(DataStoreResult.succeed(TestData.testAtoms.iterator))
    m
  }

  def publishedDataStoreMockWithTestData = {
    val m = mock[PublishedDataStore]
    when(m.getAtom(any())).thenReturn(Some(TestData.testAtoms.head))
    when(m.listAtoms).thenReturn(DataStoreResult.succeed(TestData.testAtoms.iterator))
    m
  }

  def defaultMockPublisher: LiveAtomPublisher = {
    val p = mock[LiveAtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Success(()))
    p
  }

  def defaultPreviewMockPublisher: PreviewAtomPublisher = {
    val p = mock[PreviewAtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Success(()))
    p
  }

  def failingMockPublisher: LiveAtomPublisher = {
    val p = mock[LiveAtomPublisher]
    when(p.publishAtomEvent(any())).thenReturn(Failure(new Exception("failure")))
    p
  }

  def initialPreviewDataStore = previewDataStoreMockWithTestData
  def initialPublishedDataStore = publishedDataStoreMockWithTestData
  def initialLivePublisher = mock[LiveAtomPublisher]
  def initialPreviewPublisher = mock[PreviewAtomPublisher]

  def customOverrides: Seq[Binding[_]] = Seq.empty
  def customConfig: Map[String, Any] = Map.empty

  protected def ibind[A : ClassTag](a: A): Binding[A] = bind[A] toInstance a
  // bind to a simple mock
  protected def mbind[A <: AnyRef : ClassTag : Manifest](modifier: A => Any):
      Binding[A] = {
    val mockA = mock[A]
    modifier(mockA)
    ibind[A](mockA)
  }
  protected def mbind[A <: AnyRef : ClassTag : Manifest]: Binding[A] =
    mbind[A]((a: A) => ())

  case class AtomTestConf(
    previewDataStore: PreviewDataStore = initialPreviewDataStore,
    publishedDataStore: PublishedDataStore = initialPublishedDataStore,
    livePublisher: LiveAtomPublisher = initialLivePublisher,
    previewPublisher: PreviewAtomPublisher = initialPreviewPublisher,
    shutDownHook: AtomTestConf => Unit = _.app.stop) {

    private def makeOverrides: GuiceableModule = Seq(
      ibind(dataStore),
      ibind(previewDataStore),
      ibind(publishedDataStore),
      ibind(livePublisher),
      ibind(previewPublisher)
    ) ++ customOverrides

    lazy val guicer = new GuiceApplicationBuilder()
      .configure(customConfig)
      .overrides(makeOverrides)

    lazy val app = guicer.build()

    def iget[A](implicit c: ClassTag[A]): A = app.injector.instanceOf[A]

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
