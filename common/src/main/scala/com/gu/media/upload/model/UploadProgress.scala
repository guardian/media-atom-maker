package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format

case class UploadProgress(
    chunksInS3: Int,
    chunksInYouTube: Int,
    fullyUploaded: Boolean,
    fullyTranscoded: Boolean,
    retries: Int,
    copyProgress: Option[CopyProgress] = None
)

case class CopyETag(number: Int, eTag: String)
case class CopyProgress(
    copyId: String,
    fullyCopied: Boolean,
    eTags: List[CopyETag]
)

object UploadProgress {
  implicit val format: Format[UploadProgress] =
    Jsonx.formatCaseClass[UploadProgress]
}

object CopyETag {
  implicit val format: Format[CopyETag] = Jsonx.formatCaseClass[CopyETag]
}

object CopyProgress {
  implicit val format: Format[CopyProgress] =
    Jsonx.formatCaseClass[CopyProgress]
}
