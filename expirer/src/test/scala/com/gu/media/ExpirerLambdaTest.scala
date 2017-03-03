package com.gu.media

import com.gu.media.expirer.ExpirerLambda
import org.scalatest.{FunSuite, MustMatchers}
import play.api.libs.json.{JsValue, Json}

class ExpirerLambdaTest extends FunSuite with CapiResponses with MustMatchers {
  test("Make YouTube assets private") {
    val result = capiResult(List(expiredAtom(youTubeAsset("one"), youTubeAsset("two"))))
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
  }

  test("Not touch other assets") {
    val result = capiResult(List(expiredAtom(youTubeAsset("one"), nonYouTubeAsset("two"))))
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one"))
  }

  test("Not touch other atoms") {
    val result = capiResult(List(expiredAtom(youTubeAsset("one")), liveAtom(youTubeAsset("two"))))
    val lambda = new TestExpirerLambda(List(result))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one"))
  }

  test("Iterate through CAPI pages") {
    val pageOne = capiResult(List(expiredAtom(youTubeAsset("one"))), page = 1, pages = 2)
    val pageTwo = capiResult(List(expiredAtom(youTubeAsset("two"))), page = 2, pages = 2)
    val lambda = new TestExpirerLambda(List(pageOne, pageTwo))

    lambda.handleRequest((), null)
    lambda.madePrivate must be(List("one", "two"))
  }

  class TestExpirerLambda(var capiResults: List[String]) extends ExpirerLambda with TestSettings {
    var madePrivate = List.empty[String]

    override def expireInParallel = false

    override def capiQuery(query: String): JsValue = {
      val ret = capiResults.head
      capiResults = capiResults.tail

      Json.parse(ret)
    }

    override def setStatusToPrivate(id: String): Unit = {
      madePrivate :+= id
    }
  }
}
