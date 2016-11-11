package data

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json._

object JsonConversions {

  implicit val atomType = Writes[AtomType](at => JsString(at.name.toLowerCase))
  implicit val atomTypeRead = Reads[AtomType](json => JsSuccess(AtomType.valueOf(json.as[String]).get))

  implicit val category = Writes[Category](category => JsString(category.name.toLowerCase))
  implicit val categoryRead = Reads[Category](json => JsSuccess(Category.valueOf(json.as[String]).get))

  implicit val mediaAssetFormat = Jsonx.formatSealed[Asset]
  implicit val mediaMetadataFormat = Jsonx.formatSealed[Metadata]
  implicit val mediaDataFormat = Jsonx.formatSealed[AtomData.Media]

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

  implicit val atomRead: Reads[Atom] = (
    (__ \ "id").readNullable[String] and
    (__ \ "type").readNullable[AtomType] and
    (__ \ "labels").readNullable[Seq[String]] and
    (__ \ "defaultHtml").readNullable[String] and
    (__ \ "data").readNullable[AtomData] and
    (__ \ "contentChangeDetails").readNullable[ContentChangeDetails]
    )(Atom)

  implicit val atomWrites: Writes[Atom] = (
    (__ \ "id").write[String] and
      (__ \ "type").write[AtomType] and
      (__ \ "labels").write[Seq[String]] and
      (__ \ "defaultHtml").write[String] and
      (__ \ "data").write[AtomData] and
      (__ \ "contentChangeDetails").write[ContentChangeDetails]

  ) { atom: Atom =>
    (atom.id, atom.atomType, atom.labels, atom.defaultHtml, atom.data, atom.contentChangeDetails)
  }
}
