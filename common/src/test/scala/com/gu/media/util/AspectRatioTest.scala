package com.gu.media.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AspectRatioTest extends AnyFlatSpec with Matchers {

  "calculate" should "produce an aspect ratio from dimensions" in {
    AspectRatio.calculate(1280, 720).map(_.name) should contain("16:9")
    AspectRatio.calculate(720, 1280).map(_.name) should contain("9:16")
    AspectRatio.calculate(1024, 1280).map(_.name) should contain("4:5")
    AspectRatio.calculate(1280, 1024).map(_.name) should contain("5:4")
  }

  "calculate" should "apply a tolerance to match inexact dimensions" in {
    AspectRatio.calculate(1280, 720, tolerance = 3).map(_.name) should contain(
      "16:9"
    )
    AspectRatio.calculate(1280, 721, tolerance = 3).map(_.name) should contain(
      "16:9"
    )
    AspectRatio.calculate(1280, 722, tolerance = 3).map(_.name) should contain(
      "16:9"
    )
    AspectRatio.calculate(1280, 723, tolerance = 3).map(_.name) should contain(
      "16:9"
    )
    AspectRatio.calculate(1280, 724, tolerance = 3).map(_.name) shouldBe empty
  }

}
