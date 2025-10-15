package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format

case class UploadCredentials(
    temporaryAccessId: String,
    temporarySecretKey: String,
    sessionToken: String
)

object UploadCredentials {
  implicit val format: Format[UploadCredentials] =
    Jsonx.formatCaseClass[UploadCredentials]
}

case class UploadCredentialsRequest(
    atomId: String
)
