package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadRequest(atomId: String, filename: String, size: Long, selfHost: Boolean, syncWithPluto: Boolean)

object UploadRequest {
  implicit val format: Format[UploadRequest] = Jsonx.formatCaseClass
}
