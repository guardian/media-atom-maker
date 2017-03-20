package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

sealed abstract class UploadAction
case class UploadPartToYouTube(uploadId: String, key: String) extends UploadAction
case class DeleteParts(uploadId: String, partsToDelete: List[String]) extends UploadAction

object UploadPartToYouTube {
  implicit val format: Format[UploadPartToYouTube] = Jsonx.formatCaseClass[UploadPartToYouTube]
}

object DeleteParts {
  implicit val format: Format[DeleteParts] = Jsonx.formatCaseClass[DeleteParts]
}

object UploadAction {
  implicit val format: Format[UploadAction] = Jsonx.formatSealed[UploadAction]
}
