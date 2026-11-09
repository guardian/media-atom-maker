package util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class CORSableTest extends AnyFlatSpec with Matchers {
  private val allowed = "https://allowed.test.co.uk"

  private val action: Action[AnyContent] =
    CORSable(allowed) {
      stubControllerActionBuilder(Ok("body"))
    }

  private def stubControllerActionBuilder(result: play.api.mvc.Result) =
    stubControllerComponents().actionBuilder(result)

  private def call(origin: String) =
    action(FakeRequest().withHeaders("Origin" -> origin))

  "CORSable" should "add CORS headers for an exactly matching origin" in {
    val result = call(allowed)
    header("Access-Control-Allow-Origin", result) shouldBe Some(allowed)
    header("Access-Control-Allow-Credentials", result) shouldBe Some("true")
    header("Vary", result) shouldBe Some("Origin")
  }

  it should "not add CORS headers for look-alike origins" in {
    Seq(
      "https://evilallowed.test.co.uk",
      "https://allowed.test.co.uk.evil.com",
      "http://allowed.test.co.uk"
    ).foreach { origin =>
      val result = call(origin)
      header("Access-Control-Allow-Origin", result) shouldBe None
      header("Access-Control-Allow-Credentials", result) shouldBe None
    }
  }
}
