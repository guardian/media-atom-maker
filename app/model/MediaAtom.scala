package model

import com.gu.contentatom.thrift.atom.media.{MediaAtom => ThriftMediaAtom, Metadata => ThriftMetadata, PlutoData => ThriftPlutoData}
import com.gu.contentatom.thrift.{AtomData, Atom => ThriftAtom, AtomType => ThriftAtomType, Flags => ThriftFlags}
import org.cvogt.play.json.Jsonx
import util.atom.MediaAtomImplicits

// Note: This is *NOT* structured like the thrift representation
case class MediaAtom(
  // Atom wrapper fields
  id: String,
  labels: List[String],
  contentChangeDetails: ContentChangeDetails,
  // data field
  assets: List[Asset],
  activeVersion: Option[Long],
  title: String,
  category: Category,
  plutoData: Option[PlutoData],
  duration: Option[Long],
  source: Option[String],
  description: Option[String],
  trailText: Option[String],
  posterImage: Option[Image],
  // metadata
  tags: List[String],
  byline: List[String],
  commissioningDesks: List[String],
  keywords: List[String],
  youtubeCategoryId: Option[String],
  license: Option[String],
  channelId: Option[String],
  commentsEnabled: Boolean = false,
  legallySensitive: Option[Boolean],
  sensitive: Option[Boolean],
  privacyStatus: Option[PrivacyStatus],
  expiryDate: Option[Long] = None,
  blockAds: Option[Boolean]) {

  def asThrift = ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = generateHtml(),
      title = Some(title),
      data = AtomData.Media(ThriftMediaAtom(
        assets = assets.map(_.asThrift),
        activeVersion = activeVersion,
        title = title,
        category = category.asThrift,
        duration = duration,
        source = source,
        posterUrl = posterImage.flatMap(_.master).map(_.file),
        description = description,
        trailText = trailText,
        posterImage = posterImage.map(_.asThrift),
        byline = Some(byline),
        commissioningDesks = Some(commissioningDesks),
        keywords = Some(keywords),
        metadata = Some(ThriftMetadata(
          tags = Some(tags),
          categoryId = youtubeCategoryId,
          license = license,
          commentsEnabled = Some(commentsEnabled),
          channelId = channelId,
          privacyStatus = privacyStatus.flatMap(_.asThrift),
          expiryDate = expiryDate,
          pluto = plutoData.map(_.asThrift)
          ))
        )),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = Some(ThriftFlags(
        legallySensitive = legallySensitive,
        blockAds = blockAds,
        sensitive = sensitive
      ))
  )

  def getActiveAsset = this.assets.find(_.version == this.activeVersion.get)

  private def generateHtml(): String = {
    val activeAssets = assets filter (asset => activeVersion.contains(asset.version))
    if (activeAssets.nonEmpty && activeAssets.forall(_.platform == Platform.Url)) {
      views.html.MediaAtom.embedUrlAssets2(posterImage.flatMap(_.master).map(_.file).getOrElse(""), activeAssets).toString
    } else {
      activeAssets.headOption match {
        case Some(activeAsset) if activeAsset.platform == Platform.Youtube =>
          views.html.MediaAtom.embedYoutubeAsset2(activeAsset).toString
        case _ => "<div></div>"
      }
    }
  }
}

object MediaAtom extends MediaAtomImplicits {
  implicit val mediaAtomFormat = Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.tdata

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      contentChangeDetails = ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = data.assets.map(Asset.fromThrift).toList,
      activeVersion = data.activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoData = data.metadata.flatMap(_.pluto).map(PlutoData.fromThrift),
      duration = data.duration,
      source = data.source,
      posterImage = data.posterImage.map(Image.fromThrift),
      description = data.description,
      trailText = data.trailText,
      tags = data.metadata.flatMap(_.tags.map(_.toList)).getOrElse(Nil),
      byline = data.byline.map(_.toList).getOrElse(Nil),
      commissioningDesks = data.commissioningDesks.map(_.toList).getOrElse(Nil),
      keywords = data.keywords.map(_.toList).getOrElse(Nil),
      youtubeCategoryId = data.metadata.map(_.categoryId).getOrElse(None),
      expiryDate = data.metadata.map(_.expiryDate).getOrElse(None),
      blockAds = atom.flags.flatMap(_.blockAds),
      license = data.metadata.flatMap(_.license),
      commentsEnabled = data.metadata.flatMap(_.commentsEnabled).getOrElse(false),
      channelId = data.metadata.flatMap(_.channelId),
      legallySensitive = atom.flags.flatMap(_.legallySensitive),
      sensitive = atom.flags.flatMap(_.sensitive),
      privacyStatus = data.metadata.flatMap(_.privacyStatus).flatMap(PrivacyStatus.fromThrift)
    )
  }

  def getActiveYouTubeAsset(mediaAtom: MediaAtom): Option[Asset] = {
    val assets = mediaAtom.assets
    val activeAsset = mediaAtom.activeVersion.flatMap(activeVersion => assets.find(_.version == activeVersion))

    activeAsset match {
      case Some(asset) if asset.platform == Platform.Youtube => Some(asset)
      case _ => None
    }
  }
}
