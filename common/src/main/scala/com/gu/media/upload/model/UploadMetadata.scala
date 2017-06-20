package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.media.model.VideoAsset

case class UploadMetadata(
  user: String,
  bucket: String,
  region: String,
  title: String,
  channel: String,
  pluto: PlutoSyncMetadata,
  selfHost: Boolean = false,
  asset: Option[VideoAsset] = None,
  youTubeUploadUri: Option[String] = None,
  mp4TranscodeJobId: Option[String] = None
)

case class PlutoSyncMetadata (
  enabled: Boolean,
  projectId: Option[String],
  s3Key: String,
  assetVersion: Long,
  atomId: String
)

object UploadMetadata {
  implicit val format: Format[UploadMetadata] = Jsonx.formatCaseClass[UploadMetadata]
}

object PlutoSyncMetadata {
  implicit val format: Format[PlutoSyncMetadata] = Jsonx.formatCaseClass[PlutoSyncMetadata]
}
