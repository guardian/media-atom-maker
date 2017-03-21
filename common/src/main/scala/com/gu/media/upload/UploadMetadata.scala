package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadMetadata(atomId: String, user: String, bucket: String, region: String, title: String, pluto: PlutoSyncMetadata)
case class PlutoSyncMetadata(projectId: Option[String], key: String, assetVersion: Long)

object UploadMetadata {
  implicit val format: Format[UploadMetadata] = Jsonx.formatCaseClass[UploadMetadata]
}

object PlutoSyncMetadata {
  implicit val format: Format[PlutoSyncMetadata] = Jsonx.formatCaseClass[PlutoSyncMetadata]
}
