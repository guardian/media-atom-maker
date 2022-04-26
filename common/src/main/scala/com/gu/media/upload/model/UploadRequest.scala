package com.gu.media.upload.model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class UploadRequest(
  atomId: String,
  filename: String,
  size: Long,
  selfHost: Boolean
)

object UploadRequest {
  implicit val format: Format[UploadRequest] = Jsonx.formatCaseClass
}
