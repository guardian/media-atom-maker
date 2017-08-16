package model

import com.gu.media.Permissions
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class Presence(domain: String, firstName: String, lastName: String, email: String) {
  val jsLocation = s"https://$domain/client/1/lib.js"
}

object Presence {
  implicit val format: Format[Presence] = Jsonx.formatCaseClass[Presence]
}

case class ClientConfig(
  presence: Option[Presence],
  youtubeEmbedUrl: String,
  youtubeThumbnailUrl: String,
  reauthUrl: String,
  gridUrl: String,
  capiProxyUrl: String,
  liveCapiProxyUrl: String,
  composerUrl: String,
  ravenUrl: String,
  stage: String,
  viewerUrl: String,
  // permissions also validated server-side on every request
  permissions: Permissions,
  minDurationForAds: Long,
  isTrainingMode: Boolean
)

object ClientConfig {
  implicit val format: Format[ClientConfig] = Jsonx.formatCaseClass[ClientConfig]
}
