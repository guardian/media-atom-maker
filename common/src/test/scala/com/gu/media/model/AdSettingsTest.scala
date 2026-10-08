package com.gu.media.model

import com.gu.media.TestHelpers
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class AdSettingsTest extends AnyFunSuite with Matchers {
  private val initialAtom: MediaAtom = TestHelpers.emptyAppMediaAtom

  private val minDurationForAds = 30L
  private val minDurationForMidroll = 8 * 60L

  test("creation of AdSettings when blockAds is true") {
    val previewAtom = initialAtom.copy(blockAds = true)
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }

  test(
    "creation of AdSettings when blockAds is true and video duration is set"
  ) {
    val previewAtom =
      initialAtom.copy(blockAds = true, duration = Some(minDurationForAds + 10))
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }

  test("creation of AdSettings when video is too short") {
    val previewAtom = initialAtom.copy(
      blockAds = false,
      duration = Some(minDurationForAds - 10)
    )
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }

  test("creation of AdSettings when video is Paid") {
    val previewAtom =
      initialAtom.copy(category = Category.Paid, blockAds = false)
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }

  test(
    "creation of AdSettings when video duration is greater than duration for ads but less than duration for mid roll"
  ) {
    val previewAtom =
      initialAtom.copy(blockAds = false, duration = Some(5 * 60L))
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings(blockAds = false, enableMidroll = false)
    )
  }

  test(
    "creation of AdSettings when video duration is greater than duration for ads and greater than duration for mid roll"
  ) {
    val previewAtom = initialAtom.copy(
      blockAds = false,
      duration = Some(minDurationForMidroll + 10)
    )
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings(blockAds = false, enableMidroll = true)
    )
  }

  test(
    "creation of AdSettings when video duration is greater than duration for ads but less than duration for mid roll and blockAds is true"
  ) {
    val previewAtom =
      initialAtom.copy(blockAds = true, duration = Some(5 * 60L))
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }

  test(
    "creation of AdSettings when video duration is greater than duration for ads and greater than duration for mid roll and blockAds is true"
  ) {
    val previewAtom = initialAtom.copy(
      blockAds = true,
      duration = Some(minDurationForMidroll + 10)
    )
    AdSettings(minDurationForAds, minDurationForMidroll, previewAtom) must be(
      AdSettings.NONE
    )
  }
}
