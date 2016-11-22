package model

import com.google.api.services.youtube.model.Channel
import play.api.libs.json._

case class YouTubeChannel(name: String, logo: String, id: String)

object YouTubeChannel {
  implicit val reads: Reads[YouTubeChannel] = Json.reads[YouTubeChannel]
  implicit val writes: Writes[YouTubeChannel] = Json.writes[YouTubeChannel]

  def build(channel: Channel): YouTubeChannel = {

    YouTubeChannel(
      name = channel.getSnippet().getTitle(),
      logo = channel.getSnippet().getThumbnails().getDefault().getUrl(),
      id = channel.getId
    )
  }
}
