package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format
import com.gu.media.model.{
  IconikData,
  PlutoSyncMetadataMessage,
  VideoAsset,
  VideoSource
}

case class UploadMetadata(
    user: String,
    bucket: String,
    region: String,
    title: String,
    pluto: PlutoSyncMetadataMessage,
    iconikData: Option[IconikData],
    runtime: RuntimeUploadMetadata,
    version: Option[Long] = None,
    selfHost: Boolean = false,
    asset: Option[VideoAsset] = None,
    originalFilename: Option[String] = None,
    startTimestamp: Option[Long] = None, // unix timestamp
    subtitleSource: Option[VideoSource] = None,
    subtitleVersion: Option[Long] = None
)

sealed abstract class RuntimeUploadMetadata
case class YouTubeUploadMetadata(channel: String, uri: Option[String])
    extends RuntimeUploadMetadata
case class SelfHostedUploadMetadata(jobs: List[String])
    extends RuntimeUploadMetadata

object UploadMetadata {
  implicit val format: Format[UploadMetadata] =
    Jsonx.formatCaseClass[UploadMetadata]
}

object RuntimeUploadMetadata {
  implicit val youTubeFormat: Format[YouTubeUploadMetadata] =
    Jsonx.formatCaseClass[YouTubeUploadMetadata]
  implicit val selfHostedFormat: Format[SelfHostedUploadMetadata] =
    Jsonx.formatCaseClass[SelfHostedUploadMetadata]
  implicit val format: Format[RuntimeUploadMetadata] =
    Jsonx.formatSealed[RuntimeUploadMetadata]
}
