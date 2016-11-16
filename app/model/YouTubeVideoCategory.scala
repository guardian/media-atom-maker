package model

import com.google.api.services.youtube.model.VideoCategory
import play.api.libs.json._

case class YouTubeVideoCategory (
  id: Int,
  title: String
)

object YouTubeVideoCategory {
  implicit val reads: Reads[YouTubeVideoCategory] = Json.reads[YouTubeVideoCategory]
  implicit val writes: Writes[YouTubeVideoCategory] = Json.writes[YouTubeVideoCategory]

  def build(category: VideoCategory): YouTubeVideoCategory = {
    YouTubeVideoCategory(category.getId.toInt, category.getSnippet.getTitle)
  }
}
