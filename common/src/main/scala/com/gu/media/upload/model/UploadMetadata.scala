package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.media.model.VideoAsset

case class UploadMetadata(
  user: String,
  bucket: String,
  region: String,
  title: String,
  pluto: PlutoSyncMetadata,
  selfHost: Boolean = false,
  runtime: RuntimeUploadMetadata,
  asset: Option[VideoAsset] = None,
  originalFilename: Option[String] = None
)

case class PlutoSyncMetadata (
  enabled: Boolean,
  projectId: Option[String],
  s3Key: String,
  assetVersion: Long,
  atomId: String,
  title: String,
  user: String,
  posterImageUrl: Option[String]
)

sealed abstract class RuntimeUploadMetadata
case class YouTubeUploadMetadata(channel: String, uri: Option[String]) extends RuntimeUploadMetadata
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
