package model

import org.cvogt.play.json.Jsonx
import org.joda.time.DateTime

import com.gu.contentatom.thrift.{
Atom => ThriftAtom,
AtomType => ThriftAtomType,
AtomData,
User => ThriftUser,
ContentChangeDetails => ThriftContentChangeDetails,
ChangeRecord => ThriftChangeRecord
}

import com.gu.contentatom.thrift.atom.media.{
Asset => ThriftAsset,
AssetType => ThriftAssetType,
Platform => ThriftPlatform,
MediaAtom => ThriftMediaAtom,
Category => ThriftCategory,
Metadata => ThriftMetadata
}
import play.api.libs.json._

import _root_.util.atom.MediaAtomImplicits

// Note: This is *NOT* structured like the thrift representation
case class MediaAtom(
  // Atom wrapper fields
  id: String,
  labels: List[String],
  changeDetails: ContentChangeDetails,
  // data field
  assets: List[Asset],
  activeVersion: Option[Long],
  title: String,
  category: Category,
  plutoProjectId: Option[String],
  duration: Option[Long],
  source: Option[String],
  posterUrl: Option[String],
  description: Option[String],
  // metadata
  tags: Option[List[String]],
  categoryId: Option[String],
  license: Option[String],
  commentsEnabled: Option[Boolean],
  channelId: Option[String]
  ) extends MediaAtomImplicits {

  def asThrift = ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = generateHtml(),
      data = ThriftMediaAtom(
        assets = assets.map(_.asThrift),
        activeVersion = activeVersion,
        title = title,
        category = category.asThrift,
        plutoProjectId = plutoProjectId,
        duration = duration,
        source = source,
        posterUrl = posterUrl,
        description = description,
        metadata = Some(ThriftMetadata(
          tags = tags,
          categoryId = categoryId,
          license = license,
          commentsEnabled = commentsEnabled,
          channelId = channelId
          ))
        ).asInstanceOf[AtomData],
      contentChangeDetails = changeDetails.asThrift,
      flags = None
  )

  private def generateHtml(): String = {
    "<div></div>"
  }
}

sealed trait AssetType {
  def name: String
  def asThrift = ThriftAssetType.valueOf(name).get
}

object AssetType {
  case object AUDIO extends AssetType { val name = "AUDIO" }
  case object VIDEO extends AssetType { val name = "VIDEO" }

  val assetTypeReads = Reads[AssetType](json => {
    json.as[String] match {
      case "AUDIO" => JsSuccess(AUDIO)
      case "VIDEO" => JsSuccess(VIDEO)
    }
  })

  val assetTypeWrites = Writes[AssetType] (cat => {
    JsString(cat.name)
  })

  implicit val assetTypeFormat = Format(assetTypeReads, assetTypeWrites)

  private val types = List(AUDIO, VIDEO)

  def fromThrift(p: ThriftAssetType) = types.find(_.name == p.name).get
}

case class User(email: String, firstName: Option[String], lastName: Option[String]) {
  def asThrift = ThriftUser(email, firstName, lastName)
}

object User {
  implicit val userFormat = Jsonx.formatCaseClassUseDefaults[User]
  def fromThrift(user: ThriftUser) = User(user.email, user.firstName, user.lastName)
}

case class ChangeRecord(date: DateTime, user: Option[User]) {
  def asThrift = ThriftChangeRecord(date.getMillis, user.map(_.asThrift))
}

object ChangeRecord {
  implicit val changeRecordFormat = Jsonx.formatCaseClassUseDefaults[ChangeRecord]
  def fromThrift(cr: ThriftChangeRecord) = ChangeRecord(new DateTime(cr.date), cr.user.map(User.fromThrift))
}

sealed trait Category {
  def name: String

  def asThrift = ThriftCategory.valueOf(name).get
}

object Category {
  case object DOCUMENTARY extends Category { val name = "DOCUMENTARY" }
  case object EXPLAINER extends Category { val name = "EXPLAINER" }
  case object FEATURE extends Category { val name = "FEATURE" }
  case object HOSTED extends Category { val name = "HOSTED" }
  case object NEWS extends Category { val name = "NEWS" }

  val categoryReads = Reads[Category](json => {
    json.as[String] match {
        case "DOCUMENTARY" => JsSuccess(DOCUMENTARY)
        case "EXPLAINER" => JsSuccess(DOCUMENTARY)
        case "FEATURE" => JsSuccess(FEATURE)
        case "HOSTED" => JsSuccess(HOSTED)
        case "NEWS" => JsSuccess(NEWS)
      }
    })

  val categoryWrites = Writes[Category] (cat => {
    JsString(cat.name)
  })

  implicit val categoryFormat = Format(categoryReads, categoryWrites)

  private val types = List(DOCUMENTARY, EXPLAINER, FEATURE, HOSTED, NEWS)

  def fromThrift(cat: ThriftCategory) = types.find(_.name == cat.name).get
}

case class ContentChangeDetails(lastModified: Option[ChangeRecord], created: Option[ChangeRecord], published: Option[ChangeRecord], revision: Long) {
  def asThrift = ThriftContentChangeDetails(lastModified.map(_.asThrift), created.map(_.asThrift), published.map(_.asThrift), revision)
}

object ContentChangeDetails {
  implicit val contentChangeDetailsFormat = Jsonx.formatCaseClass[ContentChangeDetails]

  def fromThrift(ccd: ThriftContentChangeDetails) = ContentChangeDetails(
    ccd.lastModified.map(ChangeRecord.fromThrift),
    ccd.created.map(ChangeRecord.fromThrift),
    ccd.published.map(ChangeRecord.fromThrift),
    ccd.revision)
}

sealed trait Platform {
  def name: String
  def asThrift = ThriftPlatform.valueOf(name).get
}

object Platform {
  case object YOUTUBE extends Platform { val name = "YOUTUBE" }
  case object FACEBOOK extends Platform { val name = "FACEBOOK" }
  case object DAILYMOTION extends Platform { val name = "DAILYMOTION" }
  case object MAINSTREAM extends Platform { val name = "MAINSTREAM" }
  case object URL extends Platform { val name = "URL" }

  val platformReads = Reads[Platform](json => {
    json.as[String] match {
      case "YOUTUBE" => JsSuccess(YOUTUBE)
      case "FACEBOOK" => JsSuccess(FACEBOOK)
      case "DAILYMOTION" => JsSuccess(DAILYMOTION)
      case "MAINSTREAM" => JsSuccess(MAINSTREAM)
      case "URL" => JsSuccess(URL)
    }
  })

  val platformWrites = Writes[Platform] (cat => {
    JsString(cat.name)
  })

  implicit val platformFormat = Format(platformReads, platformWrites)

  private val types = List(YOUTUBE, FACEBOOK, DAILYMOTION, MAINSTREAM, URL)

  def fromThrift(p: ThriftPlatform) = types.find(_.name == p.name).get
}

case class Asset(assetType: AssetType,
                 version: Long,
                 id: String,
                 platform: Platform,
                 mimeType: Option[String]) {
  def asThrift = ThriftAsset.apply(AssetType.VIDEO.asThrift, version, id, platform.asThrift, mimeType)
}

object Asset {
  implicit val assetFormat = Jsonx.formatCaseClass[Asset]
  def fromThrift(asset: ThriftAsset) = Asset(AssetType.fromThrift(asset.assetType), asset.version, asset.id, Platform.fromThrift(asset.platform), asset.mimeType)
}

object MediaAtom {
  implicit val mediaAtomFormat = Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.data.asInstanceOf[ThriftMediaAtom]

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      changeDetails = ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = data.assets.map(Asset.fromThrift).toList,
      activeVersion = data.activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoProjectId = data.plutoProjectId,
      duration = data.duration,
      source = data.source,
      posterUrl = data.posterUrl,
      description = data.description,
      tags = data.metadata.flatMap(_.tags.map(_.toList)),
      categoryId = data.metadata.flatMap(_.categoryId),
      license = data.metadata.flatMap(_.license),
      commentsEnabled = data.metadata.flatMap(_.commentsEnabled),
      channelId = data.metadata.flatMap(_.channelId)
    )
  }
}
