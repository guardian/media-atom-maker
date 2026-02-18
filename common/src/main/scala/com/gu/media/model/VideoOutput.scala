package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

sealed abstract class VideoOutput

case class YouTubeOutput(id: String) extends VideoOutput
case class SelfHostedOutput(
                             id: String,
                             mimeType: String,
                             height: Option[Int] = None,
                             width: Option[Int] = None
) extends VideoOutput

object VideoOutput {
  implicit val formatYouTube: Format[YouTubeOutput] =
    Jsonx.formatCaseClass[YouTubeOutput]
  implicit val formatSelfHosted: Format[SelfHostedOutput] =
    Jsonx.formatCaseClass[SelfHostedOutput]
  implicit val format: Format[VideoOutput] = Jsonx.formatSealed[VideoOutput]
}
