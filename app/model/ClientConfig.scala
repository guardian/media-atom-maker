package model

import com.gu.media.Permissions
import org.cvogt.play.json.Jsonx

case class ClientConfig(username: String,
                        youtubeEmbedUrl: String,
                        youtubeThumbnailUrl: String,
                        reauthUrl: String,
                        gridUrl: String,
                        capiProxyUrl: String,
                        composerUrl: String,
                        ravenUrl: String,
                        stage: String,
                        viewerUrl: String,
                        // permissions also validated server-side on every request
                        permissions: Permissions
                       )

object ClientConfig {
  implicit val clientConfigFormat = Jsonx.formatCaseClass[ClientConfig]
}
