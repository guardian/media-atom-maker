package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class Upload(id: String, atomId: String, parts: List[UploadPart])

object Upload {
  implicit val format: Format[Upload] = Jsonx.formatCaseClass[Upload]
}
