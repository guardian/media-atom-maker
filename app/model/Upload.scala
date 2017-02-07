package model

import org.cvogt.play.json.Jsonx
import play.api.libs.json._

case class UploadPolicy(bucket: String, key: String, region: String, credentials: UploadCredentials)
case class UploadCredentials(temporaryAccessKey: String, temporarySecretKey: String, sessionToken: String)

object UploadPolicy {
  implicit val format: Format[UploadPolicy] = Jsonx.formatCaseClass[UploadPolicy]
}

object UploadCredentials {
  implicit val format: Format[UploadCredentials] = Jsonx.formatCaseClass[UploadCredentials]
}

