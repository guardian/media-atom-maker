package com.gu.media

import com.amazonaws.util.IOUtils
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.expirer.ExpirerLambda
import com.gu.media.model.{AdSettings, VideoUpdateError}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsValue, Json}

class ExpirerLambdaTest extends AnyFunSuite with Matchers {
  test("Make YouTube assets private") {
    val result = capiResult("one-expired-atom-two-yt-assets.json")
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
    lambda.claimUpdated must be(List("one", "two"))
  }

  test("Not touch other assets") {
    val result =
      capiResult("one-expired-atom-one-yt-asset-one-nonyt-asset.json")
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one"))
    lambda.claimUpdated must be(List("one"))
  }

  test("Not touch third party videos") {
    val result = capiResult("one-expired-atom-two-yt-assets.json")
    val lambda = new TestExpirerLambda(List(result), isMyVideo = false)

    lambda.handleRequest((), null)
    lambda.madePrivate mustBe empty
    lambda.claimUpdated mustBe empty
  }

  test("Iterate through CAPI pages") {
    val pageOne = capiResult("one-atom-per-page-page-1.json")
    val pageTwo = capiResult("one-atom-per-page-page-2.json")
    val lambda = new TestExpirerLambda(List(pageOne, pageTwo))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
    lambda.claimUpdated must be(List("one", "two"))
  }

  class TestExpirerLambda(
      var capiResults: List[String],
      isMyVideo: Boolean = true
  ) extends ExpirerLambda
      with TestSettings {
    var madePrivate = List.empty[String]
    var claimUpdated = List.empty[String]

    override def capiQuery(
        path: String,
        qs: Map[String, String],
        queryLive: Boolean = false
    ): JsValue = {
      val ret = capiResults.head
      capiResults = capiResults.tail

      Json.parse(ret)
    }

    override def setStatus(
        id: String,
        privacyStatus: PrivacyStatus
    ): Either[VideoUpdateError, String] = {
      privacyStatus must be(PrivacyStatus.Private)
      madePrivate :+= id
      Right("")
    }

    override def createOrUpdateClaim(
        atomId: String,
        assetId: String,
        adSettings: AdSettings
    ) = {
      claimUpdated :+= assetId
      Right("")
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
