package model

import com.gu.media.model.Image
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class MediaAtomList(total: Int, atoms: List[MediaAtomSummary])
case class MediaAtomSummary(id: String, status: String, title: String, posterImage: Option[Image], contentChangeDetails: ContentChangeDetails)

object MediaAtomList {
  implicit val format: Format[MediaAtomList] = Jsonx.formatCaseClass[MediaAtomList]
}

object MediaAtomSummary {
  implicit val format: Format[MediaAtomSummary] = Jsonx.formatCaseClass[MediaAtomSummary]
}
