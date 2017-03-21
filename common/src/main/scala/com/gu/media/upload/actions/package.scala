package com.gu.media.upload

import com.gu.media.aws.{S3Access, UploadAccess}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

package object actions {
  sealed abstract class UploadAction { def uploadId: String }
  case class UploadPartToYouTube(uploadId: String, key: String) extends UploadAction
  case class CopyParts(uploadId: String, destination: String) extends UploadAction
  case class DeleteParts(uploadId: String) extends UploadAction

  type S3UploadAccess = S3Access with UploadAccess

  implicit val uploadPartToYouTubeFormat: Format[UploadPartToYouTube] = Jsonx.formatCaseClass[UploadPartToYouTube]
  implicit val copyPartsFormat: Format[CopyParts] = Jsonx.formatCaseClass[CopyParts]
  implicit val deletePartsFormat: Format[DeleteParts] = Jsonx.formatCaseClass[DeleteParts]
  implicit val uploadActionFormat: Format[UploadAction] = Jsonx.formatSealed[UploadAction]
}
