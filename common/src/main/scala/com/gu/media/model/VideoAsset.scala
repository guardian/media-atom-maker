package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class VideoSource(src: String, mimeType: String)

sealed abstract class VideoAsset
case class YouTubeAsset(id: String) extends VideoAsset
case class SelfHostedAsset(sources: List[VideoSource]) extends VideoAsset

object VideoSource {
  implicit val format: Format[VideoSource] = Jsonx.formatCaseClass[VideoSource]
}

object VideoAsset {
  implicit val formatYouTube: Format[YouTubeAsset] = Jsonx.formatCaseClass[YouTubeAsset]
  implicit val formatSelfHosted: Format[SelfHostedAsset] = Jsonx.formatCaseClass[SelfHostedAsset]
  implicit val format: Format[VideoAsset] = Jsonx.formatSealed[VideoAsset]
}
