package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadProgress(chunksInS3: Int, chunksInYouTube: Int, fullyUploaded: Boolean, fullyTranscoded: Boolean, retries: Int)

object UploadProgress {
  implicit val format: Format[UploadProgress] = Jsonx.formatCaseClass[UploadProgress]
}
