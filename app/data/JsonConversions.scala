package data

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object JsonConversions {

  implicit val atomType = Writes[AtomType](at => JsString(at.name.toLowerCase))

  implicit val mediaAsset = (
    (__ \ "id").write[String] and
      (__ \ "version").write[Long] and
      (__ \ "platform").write[String] and
      (__ \ "assetType").write[String]
  ) { asset: Asset =>
    asset match { case Asset(assetType, version, id, platform) => (id, version, platform.name, assetType.name) }
  }

  implicit val atomDataMedia = (
    (__ \ "assets").write[Seq[Asset]] and
      (__ \ "activeVersion").write[Long]
  ) { mediaAtom: MediaAtom =>
    (mediaAtom.assets, mediaAtom.activeVersion)
  }

  implicit val atomData = Writes[AtomData] {
    case AtomData.Media(mediaAtom) => Json.toJson(mediaAtom)
    case _ => JsString("unknown")
  }

  implicit val atomWrites = (
    (__ \ "id").write[String] and
      (__ \ "type").write[AtomType] and
      (__ \ "labels").write[Seq[String]] and
      (__ \ "defaultHtml").write[String] and
      (__ \ "data").write[AtomData]
  ) { atom: Atom =>
    (atom.id, atom.atomType, atom.labels, atom.defaultHtml, atom.data)
  }
  // implicit val atomWrites = (
  //   (__ \ "id").write[String] and
  //     (__ \ "atomType").write[String] and
  //     (__ \ "labels").write[Seq[String]] and
  // )

  
}
