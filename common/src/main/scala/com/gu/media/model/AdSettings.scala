package com.gu.media.model

case class AdSettings(blockAds: Boolean, enableMidroll: Boolean)

object AdSettings {
  def NONE: AdSettings = AdSettings(blockAds = true, enableMidroll = false)

  def apply(
      minDurationForAds: Long,
      minDurationForMidroll: Long,
      previewAtom: MediaAtom
  ): AdSettings = {
    previewAtom.category match {
      // GLabs atoms will always have ads blocked on YouTube,
      // so the thrift field maps to the Composer page and we don't need to check the video duration
      case Category.Hosted | Category.Paid => AdSettings.NONE
      case _ => {
        if (previewAtom.blockAds) {
          AdSettings.NONE
        } else {
          val duration = previewAtom.duration.getOrElse(0L)

          if (duration < minDurationForAds) {
            AdSettings.NONE
          } else {
            AdSettings(
              blockAds = false,
              enableMidroll = duration >= minDurationForMidroll
            )
          }
        }
      }
    }
  }
}
