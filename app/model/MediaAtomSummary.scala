package model

import com.gu.media.model.{ContentChangeDetails, Image, Platform, VideoPlayerFormat}
import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.{Format, JsObject, JsResult, JsValue, Json, OFormat}

case class MediaAtomList(total: Int, atoms: List[Temp])
sealed trait Temp
case class MediaAtomSummary(
    id: String,
    title: String,
    posterImage: Option[Image],
    contentChangeDetails: ContentChangeDetails,
    platform: Platform,
    videoPlayerFormat: Option[VideoPlayerFormat]
) extends Temp

case class FakeAtom(id: String, blah: String) extends Temp

object MediaAtomList {
  implicit val format: Format[MediaAtomList] =
    Jsonx.formatCaseClass[MediaAtomList]
}

object MediaAtomSummary {
  implicit val format: Format[MediaAtomSummary] =
    Jsonx.formatCaseClass[MediaAtomSummary]
}
object FakeAtom {
  implicit val format: Format[FakeAtom] =
    Jsonx.formatCaseClass[FakeAtom]
}

object Temp {
  implicit val format: OFormat[Temp] = new OFormat[Temp] {
    override def writes(o: Temp): JsObject = o match {
      case f: FakeAtom => Json.toJson[FakeAtom](f).as[JsObject]
      case s: MediaAtomSummary => Json.toJson[MediaAtomSummary](s).as[JsObject]
    }

    override def reads(json: JsValue): JsResult[Temp] = {
      json.validate[MediaAtomSummary]
    }
  }
}
