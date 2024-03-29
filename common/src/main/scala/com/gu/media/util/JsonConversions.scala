package com.gu.media.util

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import play.api.libs.functional.syntax._
import play.api.libs.json._

object JsonConversions {

  implicit val atomType: Writes[AtomType] = Writes[AtomType](at => JsString(at.name.toLowerCase))

  implicit val category: Writes[Category] = Writes[Category](category => JsString(category.name.toLowerCase))

  implicit val privacyStatusWrites: Writes[PrivacyStatus] = Writes[PrivacyStatus](ps => JsString(ps.name.toLowerCase))

  implicit val privacyStatusReads: Reads[PrivacyStatus] = Reads[PrivacyStatus](json => {
    json.as[String] match {
      case "private" => JsSuccess(PrivacyStatus.Private)
      case "unlisted" => JsSuccess(PrivacyStatus.Unlisted)
      case "public" => JsSuccess(PrivacyStatus.Public)
    }
  })

  implicit val mediaAsset: Writes[Asset] = (
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

  implicit val plutoReads: Reads[PlutoData] = (
    (__ \ "commissionId").readNullable[String] and
    (__ \ "projectId").readNullable[String] and
    (__ \ "masterId").readNullable[String]
  )(PlutoData.apply _)

  implicit val plutoWrites: Writes[PlutoData] = (
    (__ \ "commissionId").writeNullable[String] and
    (__ \ "projectId").writeNullable[String] and
    (__ \ "masterId").writeNullable[String]
  ) {pluto: PlutoData => (pluto.commissionId, pluto.projectId, pluto.masterId )}

  implicit val youtubeReads: Reads[YoutubeData] = (
    (__ \ "title").read[String] and
      (__ \ "description").readNullable[String]
    )(YoutubeData.apply _)

  implicit val youtubeWrites: Writes[YoutubeData] = (
    (__ \ "title").write[String] and
      (__ \ "description").writeNullable[String]
    ) { youtubeData: YoutubeData => (youtubeData.title, youtubeData.description) }

  implicit val mediaMetadata: Writes[Metadata] = (
    (__ \ "tags").writeNullable[Seq[String]] and
    (__ \ "categoryId").writeNullable[String] and
    (__ \ "license").writeNullable[String] and
    (__ \ "commentsEnabled").writeNullable[Boolean] and
    (__ \ "channelId").writeNullable[String] and
    (__ \ "privacyStatus").writeNullable[PrivacyStatus] and
    (__ \ "expiryDate").writeNullable[Long] and
    (__ \ "pluto").writeNullable[PlutoData] and
    (__ \ "youtube").writeNullable[YoutubeData]

  ) { metadata: Metadata => (
      metadata.tags.map(_.toSeq),
      metadata.categoryId,
      metadata.license,
      metadata.commentsEnabled,
      metadata.channelId,
      metadata.privacyStatus,
      metadata.expiryDate,
      metadata.pluto,
      metadata.youtube
  )}

  implicit val mediaMetadataRead: Reads[Metadata] = (
    (__ \ "tags").readNullable[Seq[String]] and
    (__ \ "categoryId").readNullable[String] and
    (__ \ "license").readNullable[String] and
    (__ \ "commentsEnabled").readNullable[Boolean] and
    (__ \ "channelId").readNullable[String] and
    (__ \ "privacyStatus").readNullable[PrivacyStatus] and
    (__ \ "expiryDate").readNullable[Long] and
    (__ \ "pluto").readNullable[PlutoData] and
    (__ \ "youtube").readNullable[YoutubeData]
  )(Metadata.apply _)

  implicit val atomDataMedia: OWrites[MediaAtom] = (
    (__ \ "assets").write[Seq[Asset]] and
    (__ \ "activeVersion").writeNullable[Long] and
    (__ \ "title").write[String] and
    (__ \ "category").write[Category] and
    (__ \ "plutoProjectId").writeNullable[String] and
    (__ \ "duration").writeNullable[Long] and
    (__ \ "posterUrl").writeNullable[String] and
    (__ \ "description").writeNullable[String] and
    (__ \ "trailText").writeNullable[String] and
    (__ \ "source").writeNullable[String] and
    (__ \ "byline").writeNullable[Seq[String]] and
    (__ \ "commissioningDesks").writeNullable[Seq[String]] and
    (__ \ "keywords").writeNullable[Seq[String]] and
    (__ \ "metadata").writeNullable[Metadata]
    ) { mediaAtom: MediaAtom =>
    (
      mediaAtom.assets.toSeq,
      mediaAtom.activeVersion,
      mediaAtom.title,
      mediaAtom.category,
      mediaAtom.plutoProjectId,
      mediaAtom.duration,
      mediaAtom.posterUrl,
      mediaAtom.description,
      mediaAtom.trailText,
      mediaAtom.source,
      mediaAtom.byline.map(_.toSeq),
      mediaAtom.commissioningDesks.map(_.toSeq),
      mediaAtom.keywords.map(_.toSeq),
      mediaAtom.metadata
      )
  }

  implicit val atomData: Writes[AtomData] = Writes[AtomData] {
    case AtomData.Media(mediaAtom) => Json.toJson(mediaAtom)
    case _ => JsString("unknown")
  }

  implicit val userWrites: OWrites[User] = (
    (__ \ "email").write[String] and
    (__ \ "firstName").writeNullable[String] and
    (__ \ "lastName").writeNullable[String]
    ) { user: User => (user.email, user.firstName, user.lastName) }

  implicit val changeRecordWrites: OWrites[ChangeRecord] = (
    (__ \ "date").write[Long] and
    (__ \ "user").writeNullable[User]
    ) { changeRecord: ChangeRecord => (changeRecord.date, changeRecord.user) }

  implicit val contentChangeDetailsWrites: OWrites[ContentChangeDetails] = (
    (__ \ "lastModified").writeNullable[ChangeRecord] and
    (__ \ "created").writeNullable[ChangeRecord] and
    (__ \ "published").writeNullable[ChangeRecord] and
    (__ \ "revision").write[Long] and
    (__ \ "scheduledLaunch").writeNullable[ChangeRecord] and
    (__ \ "embargo").writeNullable[ChangeRecord] and
    (__ \ "expiry").writeNullable[ChangeRecord]
    ) { contentChangeDetails: ContentChangeDetails =>
    (
      contentChangeDetails.lastModified,
      contentChangeDetails.created,
      contentChangeDetails.published,
      contentChangeDetails.revision,
      contentChangeDetails.scheduledLaunch,
      contentChangeDetails.embargo,
      contentChangeDetails.expiry
      )
  }

  implicit val atomWrites: Writes[Atom] = (
    (__ \ "id").write[String] and
      (__ \ "type").write[AtomType] and
      (__ \ "labels").write[Seq[String]] and
      (__ \ "defaultHtml").write[String] and
      (__ \ "data").write[AtomData] and
      (__ \ "contentChangeDetails").write[ContentChangeDetails]

  ) { atom: Atom =>
    (atom.id, atom.atomType, atom.labels.toSeq, atom.defaultHtml, atom.data, atom.contentChangeDetails)
  }
}
