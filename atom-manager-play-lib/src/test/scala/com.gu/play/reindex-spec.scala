package com.gu.atom.play.test

import com.gu.atom.play.ReindexController
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.mock.MockitoSugar.mock
import com.gu.atom.publish._

class ReindexSpec extends AtomSuite {

  def reindexCtrl(implicit c: AtomTestConf) = c.app.injector.instanceOf[ReindexController]

  override def customOverrides = mbind[AtomReindexer] :: Nil

  "reindex api" should {
    "deny access without api key" in AtomTestConf() { implicit conf =>
      val res = reindexCtrl.newReindexJob().apply(FakeRequest())
      status(res) mustEqual UNAUTHORIZED
    }
  }

}
