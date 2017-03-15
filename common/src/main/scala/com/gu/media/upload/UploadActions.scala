package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

sealed abstract class UploadAction
case class PartUploaded(uploadId: String, key: String) extends UploadAction
case class FullKeyCreated(uploadId: String, fullKey: String, partsToDelete: List[String]) extends UploadAction

object PartUploaded {
  implicit val format: Format[PartUploaded] = Jsonx.formatCaseClass[PartUploaded]
}

object FullKeyCreated {
  implicit val format: Format[FullKeyCreated] = Jsonx.formatCaseClass[FullKeyCreated]
}

object UploadAction {
  implicit val format: Format[UploadAction] = Jsonx.formatSealed[UploadAction]
}
