package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadMetadata(atomId: String, user: String, bucket: String, region: String, title: String,
                          plutoProjectId: Option[String])

object UploadMetadata {
  implicit val format: Format[UploadMetadata] = Jsonx.formatCaseClass[UploadMetadata]
}
