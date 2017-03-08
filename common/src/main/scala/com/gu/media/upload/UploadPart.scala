package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadPart(key: String, start: Long, end: Long, uploadedToS3: Long = 0, uploadedToYouTube: Long = 0)

object UploadPart {
  implicit val format: Format[UploadPart] = Jsonx.formatCaseClass[UploadPart]
}
