package model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class MediaAtomSummary(id: String, status: String, title: String, posterImage: Option[Image])

object MediaAtomSummary {
  implicit val format: Format[MediaAtomSummary] = Jsonx.formatCaseClass[MediaAtomSummary]
}
