package data

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import play.api.libs.functional.syntax._
import play.api.libs.json._

object JsonConversions {

  implicit val atomType = Writes[AtomType](at => JsString(at.name.toLowerCase))

  implicit val category = Writes[Category](category => JsString(category.name.toLowerCase))

  implicit val mediaAsset = (
    (__ \ "id").write[String] and
    (__ \ "version").write[Long] and
    (__ \ "platform").write[String] and
    (__ \ "assetType").write[String] and
    (__ \ "mimeType").writeNullable[String]
  ) { asset: Asset =>
    asset match {
      case Asset(assetType, version, id, platform, mimeType) => (id, version, platform.name, assetType.name, mimeType)
    }
  }

  implicit val atomDataMedia = (
    (__ \ "assets").write[Seq[Asset]] and
    (__ \ "activeVersion").writeNullable[Long] and
    (__ \ "title").write[String] and
    (__ \ "category").write[Category] and
    (__ \ "plutoProjectId").writeNullable[String] and
    (__ \ "duration").writeNullable[Long]
    ) { mediaAtom: MediaAtom =>
    (
      mediaAtom.assets,
      mediaAtom.activeVersion,
      mediaAtom.title,
      mediaAtom.category,
      mediaAtom.plutoProjectId,
      mediaAtom.duration
      )
  }

  implicit val atomData = Writes[AtomData] {
    case AtomData.Media(mediaAtom) => Json.toJson(mediaAtom)
    case _ => JsString("unknown")
  }

  implicit val userWrites = (
    (__ \ "email").write[String] and
    (__ \ "firstName").writeNullable[String] and
    (__ \ "lastName").writeNullable[String]
    ) { user: User => (user.email, user.firstName, user.lastName) }

  implicit val changeRecordWrites = (
    (__ \ "date").write[Long] and
    (__ \ "user").writeNullable[User]
    ) { changeRecord: ChangeRecord => (changeRecord.date, changeRecord.user) }

  implicit val contentChangeDetailsWrites = (
    (__ \ "lastModified").writeNullable[ChangeRecord] and
    (__ \ "created").writeNullable[ChangeRecord] and
    (__ \ "published").writeNullable[ChangeRecord] and
    (__ \ "revision").write[Long]
    ) { contentChangeDetails: ContentChangeDetails =>
    (
      contentChangeDetails.lastModified,
      contentChangeDetails.created,
      contentChangeDetails.published,
      contentChangeDetails.revision
      )
  }

  implicit val flagsWrites = Writes[Flags] { flags: Flags => Json.toJson(flags.suppressFurniture) }

  implicit val atomWrites: Writes[Atom] = (
    (__ \ "id").write[String] and
      (__ \ "type").write[AtomType] and
      (__ \ "labels").write[Seq[String]] and
      (__ \ "defaultHtml").write[String] and
      (__ \ "data").write[AtomData]
  ) { atom: Atom =>
    (atom.id, atom.atomType, atom.labels, atom.defaultHtml, atom.data)
  }
}
