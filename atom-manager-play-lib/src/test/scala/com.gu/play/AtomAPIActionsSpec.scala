package com.gu.atom.play.test

import com.gu.atom.TestData
import com.gu.contentatom.thrift._
import org.mockito.Mockito._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import cats.data.Xor
import com.gu.atom.play._
import play.api.mvc.Controller
import play.api.test.Helpers._
import play.api.test.FakeRequest
import org.scalatest.Inside
import java.util.Date

class AtomAPIActionsSpec extends AtomSuite with Inside {

  override def initialLivePublisher = defaultMockPublisher

  override def initialPublishedDataStore = {
    val m = publishedDataStoreMockWithTestData
    when(m.updateAtom(any())).thenReturn(Xor.Right(()))
    m
  }

  def apiActions(implicit conf: AtomTestConf) = new Controller with AtomAPIActions {
    val livePublisher = conf.livePublisher
    val previewPublisher = conf.previewPublisher
    val previewDataStore = conf.previewDataStore
    val publishedDataStore = conf.publishedDataStore
  }

  "api publish action" should {
    "succeed with NO_CONTENT" in AtomTestConf() { implicit conf =>
      val result = call(apiActions.publishAtom("1"), FakeRequest())
      status(result) mustEqual NO_CONTENT
    }

    "update publish time and version for atom" in AtomTestConf() { implicit conf =>
      val startTime = (new Date()).getTime()
      val atomCaptor = ArgumentCaptor.forClass(classOf[Atom])
      val result = call(apiActions.publishAtom("1"), FakeRequest())
      status(result) mustEqual NO_CONTENT
      verify(conf.publishedDataStore).updateAtom(atomCaptor.capture())
      
      inside(atomCaptor.getValue()) {
        case Atom("1", _, _, _, _, changeDetails, _) => {
          changeDetails.published.value.date must be >= startTime
          changeDetails.revision mustEqual 2
        }
      }
    }

  }

}
