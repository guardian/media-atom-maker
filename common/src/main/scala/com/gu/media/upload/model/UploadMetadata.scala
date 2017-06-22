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
  runtime: Option[RuntimeUploadMetadata] = None
)

case class PlutoSyncMetadata (
  enabled: Boolean,
  projectId: Option[String],
  s3Key: String,
  assetVersion: Long,
  atomId: String
)

sealed abstract class RuntimeUploadMetadata
case class YouTubeUploadMetadata(uri: String) extends RuntimeUploadMetadata
case class SelfHostedUploadMetadata(jobs: List[String]) extends RuntimeUploadMetadata

object UploadMetadata {
  implicit val format: Format[UploadMetadata] = Jsonx.formatCaseClass[UploadMetadata]
}

object PlutoSyncMetadata {
  implicit val format: Format[PlutoSyncMetadata] = Jsonx.formatCaseClass[PlutoSyncMetadata]
}

object RuntimeUploadMetadata {
  implicit val youTubeFormat: Format[YouTubeUploadMetadata] = Jsonx.formatCaseClass[YouTubeUploadMetadata]
  implicit val selfHostedFormat: Format[SelfHostedUploadMetadata] = Jsonx.formatCaseClass[SelfHostedUploadMetadata]
  implicit val format: Format[RuntimeUploadMetadata] = Jsonx.formatSealed[RuntimeUploadMetadata]
}
