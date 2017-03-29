package com.gu.media.upload

import com.gu.media.upload.model.{Upload, UploadPart}
import com.gu.media.aws.{S3Access, UploadAccess, SESSettings, KinesisAccess}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

package object actions {
  sealed abstract class UploadAction { def upload: Upload }
  case class UploadPartToYouTube(upload: Upload, part: UploadPart, uploadUri: String) extends UploadAction
  case class CopyParts(upload: Upload, destination: String) extends UploadAction
  case class DeleteParts(upload: Upload) extends UploadAction

  type UploaderAccess = S3Access with UploadAccess with SESSettings with KinesisAccess

  implicit val uploadPartToYouTubeFormat: Format[UploadPartToYouTube] = Jsonx.formatCaseClass[UploadPartToYouTube]
  implicit val copyPartsFormat: Format[CopyParts] = Jsonx.formatCaseClass[CopyParts]
  implicit val deletePartsFormat: Format[DeleteParts] = Jsonx.formatCaseClass[DeleteParts]
  implicit val uploadActionFormat: Format[UploadAction] = Jsonx.formatSealed[UploadAction]
}
