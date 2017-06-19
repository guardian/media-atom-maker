package com.gu.media

import com.amazonaws.util.IOUtils
import com.gu.media.expirer.ExpirerLambda
import org.scalatest.{FunSuite, MustMatchers}
import play.api.libs.json.{JsValue, Json}

class ExpirerLambdaTest extends FunSuite with MustMatchers {
  test("Make YouTube assets private") {
    val result = capiResult("one-expired-atom-two-yt-assets.json")
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
  }

  test("Not touch other assets") {
    val result = capiResult("one-expired-atom-one-yt-asset-one-nonyt-asset.json")
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one"))
  }

  test("Not touch other atoms") {
    val result = capiResult("one-live-atom-one-expired-atom.json")
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one"))
  }

  test("Iterate through CAPI pages") {
    val pageOne = capiResult("one-atom-per-page-page-1.json")
    val pageTwo = capiResult("one-atom-per-page-page-2.json")
    val lambda = new TestExpirerLambda(List(pageOne, pageTwo))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
  }

  class TestExpirerLambda(var capiResults: List[String]) extends ExpirerLambda with TestSettings {
    var madePrivate = List.empty[String]

    override def expireInParallel = false

    override def capiQuery(query: String, isLive: Boolean = false): JsValue = {
      val ret = capiResults.head
      capiResults = capiResults.tail

      Json.parse(ret)
    }

    override def setStatus(id: String, status: String): Unit = {
      status must be("Private")
      madePrivate :+= id
    }
  }

  private def capiResult(filename: String) = {
    val resource = getClass.getResourceAsStream('/' + filename)
    IOUtils.toString(resource)
  }
}
