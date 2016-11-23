package model

import com.google.api.services.youtube.model.Channel
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.net.URI

case class YouTubeChannel(name: String, logo: URI, id: String)

object YouTubeChannel {
  implicit val reads: Reads[YouTubeChannel] = (
    (__ \ "name").read[String] ~
    (__ \ "logo").read[String].map(URI.create) ~
    (__ \ "id").read[String]
    )(YouTubeChannel.apply _)

  implicit val writes: Writes[YouTubeChannel] = (
    (__ \ "name").write[String] ~
    (__ \ "logo").write[String].contramap((_: URI).toString) ~
    (__ \ "id").write[String]
    )(unlift(YouTubeChannel.unapply))

  def build(channel: Channel): YouTubeChannel = {

    YouTubeChannel(
      name = channel.getSnippet().getTitle(),
      logo = URI.create(channel.getSnippet().getThumbnails().getDefault().getUrl()),
      id = channel.getId
    )
  }
}
