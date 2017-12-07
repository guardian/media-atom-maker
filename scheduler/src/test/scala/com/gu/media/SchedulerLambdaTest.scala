package com.gu.media

import com.amazonaws.util.IOUtils
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.scheduler.SchedulerLambda
import org.scalatest.{FunSuite, MustMatchers}
import play.api.libs.json.{JsValue, Json}

class SchedulerLambdaTest extends FunSuite with MustMatchers {
  test("Make YouTube assets public") {
    val result = capiResult("one-scheduled-atom-two-yt-assets.json")
    val lambda = new TestSchedulerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePublic must be(List("one", "two"))
  }

  test("Not touch other assets") {
    val result = capiResult("one-scheduled-atom-one-yt-asset-one-nonyt-asset.json")
    val lambda = new TestSchedulerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePublic must be(List("one"))
  }

  test("Launch atom with scheduled launch set after embargo") {
    val result = capiResult("one-atom-scheduled-after-embargo.json")
    val lambda = new TestSchedulerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePublic must be(List("one"))
  }

  test("Not launch atom with scheduled launch set before embargo") {
    val result = capiResult("one-atom-scheduled-before-embargo.json")
    val lambda = new TestSchedulerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePublic mustBe empty
  }

  test("Not touch third party videos") {
    val result = capiResult("one-scheduled-atom-two-yt-assets.json")
    val lambda = new TestSchedulerLambda(List(result), isMyVideo = false)

    lambda.handleRequest((), null)
    lambda.madePublic mustBe empty
  }

  test("Iterate through CAPI pages") {
    val pageOne = capiResult("one-atom-per-page-page-1.json")
    val pageTwo = capiResult("one-atom-per-page-page-2.json")

    val lambda = new TestSchedulerLambda(List(pageOne, pageTwo))
    lambda.handleRequest((), null)
    lambda.madePublic must be(List("one", "two"))
  }

  class TestSchedulerLambda(var capiResults: List[String], isMyVideo: Boolean = true) extends SchedulerLambda with TestSettings {
    var madePublic = List.empty[String]

    override def scheduleInParallel = false

    override def capiQuery(query: String, isLive: Boolean = false): JsValue = {
      val ret = capiResults.head
      capiResults = capiResults.tail

      Json.parse(ret)
    }

    override def setStatus(id: String, privacyStatus: PrivacyStatus): Unit = {
      privacyStatus must be(PrivacyStatus.Public)
      madePublic :+= id
    }

    override def isManagedVideo(youtubeId: String): Boolean = {
      isMyVideo
    }
  }

  private def capiResult(filename: String) = {
    val resource = getClass.getResourceAsStream('/' + filename)
    IOUtils.toString(resource)
  }
}
