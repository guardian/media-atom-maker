package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadPart(key: String, start: Long, end: Long, uploadedToS3: Boolean = false, uploadedToYouTube: Boolean = false)

object UploadPart {
  implicit val format: Format[UploadPart] = Jsonx.formatCaseClass[UploadPart]
}
