package com.gu.media.upload.mediaconvert

case class Dimensions(width: Option[Int], height: Option[Int])

sealed trait EncodingConfig {
  def dimensions: Dimensions
  def nameModifier: String
  def qualityLevel: Int
}

object EncodingConfigs {
  case object Default extends EncodingConfig {
    val dimensions = Dimensions(None, Some(720))
    val nameModifier = "_720h"
    val qualityLevel = 8
  }

  case object MobileWidth extends EncodingConfig {
    val dimensions = Dimensions(Some(480), None)
    val nameModifier = "_480w"
    val qualityLevel = 8
  }

  case object LowQuality extends EncodingConfig {
    val dimensions = Dimensions(None, Some(720))
    val nameModifier = "_720h_q6"
    val qualityLevel = 6
  }

  case object LowQualityMobileWidth extends EncodingConfig {
    val dimensions = Dimensions(Some(480), None)
    val nameModifier = "_480w_q6"
    val qualityLevel = 6
  }

  case object VeryLowQuality extends EncodingConfig {
    val dimensions = Dimensions(None, Some(720))
    val nameModifier = "_720h_q4"
    val qualityLevel = 4
  }

  case object VeryLowQualityMobileWidth extends EncodingConfig {
    val dimensions = Dimensions(Some(480), None)
    val nameModifier = "_480w_q4"
    val qualityLevel = 4
  }
}
