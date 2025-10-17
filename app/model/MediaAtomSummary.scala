package model

import com.gu.media.model.{Image, ContentChangeDetails}
import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class MediaAtomList(total: Int, atoms: List[MediaAtomSummary])

case class MediaAtomSummary(
    id: String,
    title: String,
    posterImage: Option[Image],
    contentChangeDetails: ContentChangeDetails,
    mediaPlatforms: List[String],
    currentMediaPlatform: Option[String]
)

object MediaAtomList {
  implicit val format: Format[MediaAtomList] =
    Jsonx.formatCaseClass[MediaAtomList]
}

object MediaAtomSummary {
  implicit val format: Format[MediaAtomSummary] =
    Jsonx.formatCaseClass[MediaAtomSummary]
}
