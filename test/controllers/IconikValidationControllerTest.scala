package controllers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsBoolean, JsNull}

class IconikValidationControllerTest extends AnyFlatSpec with Matchers {

  "parentMatches" should "return unknown when the expected parent is unavailable" in {
    IconikValidationController.parentMatches(
      None,
      Some("iconik-parent")
    ) shouldBe JsNull
  }

  it should "compare the Iconik parent with a known expected parent" in {
    IconikValidationController.parentMatches(
      Some("expected-parent"),
      Some("expected-parent")
    ) shouldBe JsBoolean(true)

    IconikValidationController.parentMatches(
      Some("expected-parent"),
      Some("different-parent")
    ) shouldBe JsBoolean(false)
  }
}
