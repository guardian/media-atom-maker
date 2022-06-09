package com.gu.media.upload.model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class UploadPart(key: String, start: Long, end: Long)

object UploadPart {
  implicit val format: Format[UploadPart] = Jsonx.formatCaseClass[UploadPart]
}
