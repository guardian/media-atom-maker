package com.gu.atom.play.test

import org.mockito.ArgumentMatchers._

import com.gu.atom.play.ReindexController
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock
import com.gu.atom.publish._

class ReindexSpec extends AtomSuite {

  def reindexCtrl(implicit c: AtomTestConf) = c.app.injector.instanceOf[ReindexController]

  val reindexApiKey = "xyzzy"

  override def customOverrides = {

    super.customOverrides :+ mbind[PublishedAtomReindexer] { (r: AtomReindexer) =>
      when(r.startReindexJob(any(), any())).thenReturn(AtomReindexJob.empty)
    } :+ mbind[PreviewAtomReindexer] { (r: AtomReindexer) =>
      when(r.startReindexJob(any(), any())).thenReturn(AtomReindexJob.empty)
    }

  }
  override def customConfig = super.customConfig + ("reindexApiKey" -> reindexApiKey)

  "preview reindex api" should {
    "deny access without api key or with incorrect key" in AtomTestConf() { implicit conf =>
      (status(reindexCtrl.newPreviewReindexJob().apply(FakeRequest()))
         mustEqual UNAUTHORIZED)

      (status(reindexCtrl.newPreviewReindexJob().apply(FakeRequest("GET", s"/?api=jafklsj")))
         mustEqual UNAUTHORIZED)
    }
  }

  "publish reindex api" should {
    "deny access without api key or with incorrect key" in AtomTestConf() { implicit conf =>
      (status(reindexCtrl.newPublishedReindexJob().apply(FakeRequest()))
        mustEqual UNAUTHORIZED)

      (status(reindexCtrl.newPublishedReindexJob().apply(FakeRequest("GET", s"/?api=jafklsj")))
        mustEqual UNAUTHORIZED)
    }
  }

}
