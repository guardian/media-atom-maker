package com.gu.media.upload

import com.gu.media.upload.model.{Upload, UploadPart}
import com.gu.media.aws._
import org.cvogt.play.json.Jsonx
import play.api.libs.json._

package object actions {
  sealed abstract class UploadAction { def upload: Upload }
  case class UploadPartToYouTube(upload: Upload, part: UploadPart, uploadUri: String) extends UploadAction
  case class CopyParts(upload: Upload, destination: String) extends UploadAction
  case class UploadPartsToSelfHost(upload: Upload, fileName: String, pipelineId: String) extends UploadAction
  case class DeleteParts(upload: Upload) extends UploadAction

  type UploaderAccess = S3Access with UploadAccess with SESSettings with KinesisAccess with ElasticTranscodeAccess

  implicit val uploadPartToYouTubeFormat: Format[UploadPartToYouTube] = Jsonx.formatCaseClass[UploadPartToYouTube]
  implicit val copyPartsFormat: Format[CopyParts] = Jsonx.formatCaseClass[CopyParts]
  implicit val deletePartsFormat: Format[DeleteParts] = Jsonx.formatCaseClass[DeleteParts]
  implicit val uploadPartsToSelfHostFormat: Format[UploadPartsToSelfHost] = Jsonx.formatCaseClass[UploadPartsToSelfHost]

  implicit val uploadActionsRead: Reads[UploadAction] = Reads[UploadAction] { json =>
    (json \ "type").validate[String].flatMap {
      case "UploadPartToYouTube" => (json \ "data").validate[UploadPartToYouTube]
      case "CopyParts" => (json \ "data").validate[CopyParts]
      case "UploadPartsToSelfHost" => (json \ "data").validate[UploadPartsToSelfHost]
      case "DeleteParts" => (json \ "data").validate[DeleteParts]
      case unknown => JsError(s"Unknown UploadAction type $unknown")
    }
  }

  implicit val uploadActionsWrite: Writes[UploadAction] = Writes[UploadAction] {
    case value: UploadPartToYouTube =>
      writeHelper("UploadPartToYouTube", Json.toJson(value)(uploadPartToYouTubeFormat))

    case value: CopyParts =>
      writeHelper("CopyParts", Json.toJson(value)(copyPartsFormat))

    case value: UploadPartsToSelfHost =>
      writeHelper("UploadPartsToSelfHost", Json.toJson(value)(uploadPartsToSelfHostFormat))

    case value: DeleteParts =>
      writeHelper("DeleteParts", Json.toJson(value)(deletePartsFormat))
  }

  private def writeHelper(tpe: String, data: JsValue) = JsObject(Map("type" -> JsString(tpe), "data" -> data))
}
