package com.gu.atom.play.test

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
  override def initialDataStore = {
    val m = dataStoreMockWithTestData
    when(m.updateAtom(any())).thenReturn(Xor.Right(()))
    m
  }

  def apiActions(implicit conf: AtomTestConf) = new Controller with AtomAPIActions {
    val livePublisher = conf.livePublisher
    val previewPublisher = conf.previewPublisher
    val dataStore = conf.dataStore
  }

  "api publish action" should {
    "succeed with NO_CONTENT" in AtomTestConf() { implicit conf =>
      val result = call(apiActions.publishAtom("1"), FakeRequest())
      status(result) mustEqual NO_CONTENT
    }
    "update publish time for atom" in AtomTestConf() { implicit conf =>
      val startTime = (new Date()).getTime()
      val atomCaptor = ArgumentCaptor.forClass(classOf[Atom])
      call(apiActions.publishAtom("1"), FakeRequest())
      verify(conf.dataStore).updateAtom(atomCaptor.capture())
      inside(atomCaptor.getValue()) {
        case Atom("1", _, _, _, _, changeDetails, _) =>
          changeDetails.published.value.date must be >= startTime
      }
    }
  }

}
