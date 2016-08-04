package com.gu.atom.play.test

import com.gu.atom.play._
import play.api.mvc.Controller
import play.api.test.Helpers._
import play.api.test.FakeRequest

class AtomAPIActionsSpec extends AtomSuite {

  override def initialLivePublisher = defaultMockPublisher

  def apiActions(implicit conf: AtomTestConf) = new Controller with AtomAPIActions {
    val livePublisher = conf.livePublisher
    val previewPublisher = conf.previewPublisher
    val  dataStore = conf.dataStore
  }

  "api publish action" should {
    "call out to live publisher to publish an atom" in AtomTestConf() { implicit conf =>
      val result = call(apiActions.publishAtom("1"), FakeRequest())
      status(result) mustEqual NO_CONTENT
    }

  }

}
