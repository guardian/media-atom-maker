package model

import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json.Json
import scala.concurrent.{Future}

case class ClientConfig(username: String,
                        youtubeEmbedUrl: String,
                        youtubeThumbnailUrl: String,
                        reauthUrl: String,
                        gridUrl: String,
                        capiProxyUrl: String,
                        composerUrl: String,
                        ravenUrl: String,
                        stage: String,
                        viewerUrl: String
                       )

object ClientConfig {
  implicit val clientConfigFormat = Jsonx.formatCaseClass[ClientConfig]
}
