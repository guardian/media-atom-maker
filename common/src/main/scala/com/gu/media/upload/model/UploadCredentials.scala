package com.gu.media.upload.model

import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class UploadCredentials(temporaryAccessId: String, temporarySecretKey: String, sessionToken: String)

object UploadCredentials {
  implicit val format: Format[UploadCredentials] = Jsonx.formatCaseClass[UploadCredentials]
}

case class UploadCredentialsRequest (
  atomId: String
)
