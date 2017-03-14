package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class YouTubeMetadata(channel: String, upload: Option[String])

object YouTubeMetadata {
  implicit val format: Format[YouTubeMetadata] = Jsonx.formatCaseClass[YouTubeMetadata]
}
