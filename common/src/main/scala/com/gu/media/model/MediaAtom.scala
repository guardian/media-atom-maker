package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import com.gu.contentatom.thrift.atom.media.{
  MediaAtom => ThriftMediaAtom,
  Metadata => ThriftMetadata,
  SelfHostData => ThriftSelfHostData,
  YoutubeData => ThriftYoutubeData
}
import play.api.libs.json.{Format, OFormat, __}
import com.gu.contentatom.thrift.{
  AtomData,
  Atom => ThriftAtom,
  AtomType => ThriftAtomType,
  Flags => ThriftFlags
}
import com.gu.media.model.Platform.Url
import com.gu.media.model.VideoPlayerFormat.Loop
import com.gu.media.util.MediaAtomImplicits
import com.gu.media.youtube.{
  MediaAtomYoutubeDescriptionHandler,
  YoutubeDescription
}

abstract class MediaAtomBase {
  // generic metadata
  val title: String // aka headline
  val description: Option[String] // aka standfirst
  val posterImage: Option[Image]
  val category: Category
  val source: Option[String]
  val contentChangeDetails: ContentChangeDetails

  // youtube metadata
  val channelId: Option[String]
  val privacyStatus: Option[PrivacyStatus]
  val youtubeCategoryId: Option[String]
  val keywords: List[String]
  val license: Option[String]
  val blockAds: Boolean
  val expiryDate: Option[Long]
  val youtubeTitle: String
  val youtubeDescription: Option[String]
  val youtubeOverrideImage: Option[Image]

  // composer metadata
  val trailImage: Option[Image]
  val trailText: Option[String]
  val tags: List[String]
  val byline: List[String]
  val commissioningDesks: List[String]
  val legallySensitive: Option[Boolean]
  val sensitive: Option[Boolean]
  val optimisedForWeb: Option[Boolean]
  val composerCommentsEnabled: Option[Boolean]
  val suppressRelatedContent: Option[Boolean]
  val videoPlayerFormat: Option[VideoPlayerFormat]
  val platform: Option[Platform]

  def isOnCommercialChannel(
      commercialChannels: Set[String]
  ): Option[Boolean] = {
    channelId.map(commercialChannels.contains)
  }
}

// This is used to parse the a media atom from a create atom
// request before an id has been added to it
case class MediaAtomBeforeCreation(
    title: String,
    description: Option[String],
    posterImage: Option[Image],
    category: Category,
    source: Option[String],
    contentChangeDetails: ContentChangeDetails,
    channelId: Option[String],
    privacyStatus: Option[PrivacyStatus],
    youtubeCategoryId: Option[String],
    keywords: List[String],
    license: Option[String],
    blockAds: Boolean,
    expiryDate: Option[Long],
    youtubeOverrideImage: Option[Image],
    trailImage: Option[Image],
    trailText: Option[String],
    tags: List[String],
    byline: List[String],
    commissioningDesks: List[String],
    legallySensitive: Option[Boolean],
    sensitive: Option[Boolean],
    optimisedForWeb: Option[Boolean],
    composerCommentsEnabled: Option[Boolean],
    suppressRelatedContent: Option[Boolean],
    videoPlayerFormat: Option[VideoPlayerFormat],
    platform: Option[Platform]
) extends MediaAtomBase {

  def asThrift(id: String, contentChangeDetails: ContentChangeDetails) = {
    val data = ThriftMediaAtom(
      assets = Nil,
      activeVersion = None,
      title = title,
      category = category.asThrift,
      duration = None,
      source = source,
      posterUrl = posterImage.flatMap(_.master).map(_.file),
      description = description,
      trailText = trailText,
      posterImage = posterImage.map(_.asThrift),
      trailImage = trailImage.map(_.asThrift),
      youtubeOverrideImage = youtubeOverrideImage.map(_.asThrift),
      byline = Some(byline),
      commissioningDesks = Some(commissioningDesks),
      keywords = Some(keywords),
      metadata = Some(
        ThriftMetadata(
          tags = Some(tags),
          categoryId = youtubeCategoryId,
          license = license,
          channelId = channelId,
          privacyStatus = privacyStatus.flatMap(_.asThrift),
          expiryDate = expiryDate,
          pluto = None,
          iconik = None,
          youtube = Some(ThriftYoutubeData(youtubeTitle, youtubeDescription)),
          selfHost =
            videoPlayerFormat.map(vpf => ThriftSelfHostData(Some(vpf.asThrift)))
        )
      ),
      commentsEnabled = composerCommentsEnabled,
      optimisedForWeb = optimisedForWeb,
      suppressRelatedContent = suppressRelatedContent,
      platform = platform.map(_.asThrift)
    )

    ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = MediaAtomImplicits.defaultMediaHtml(data),
      title = Some(title),
      data = AtomData.Media(data),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = Some(
        ThriftFlags(
          legallySensitive = legallySensitive,
          blockAds = Some(blockAds),
          sensitive = sensitive
        )
      )
    )
  }

  // when creating an atom, use the `title` (headline) and `description` (standfirst) as the initial `youtubeTitle` and `youtubeDescription`
  override val youtubeTitle: String = title
  override val youtubeDescription: Option[String] =
    YoutubeDescription.clean(description)
}

object MediaAtomBeforeCreation {
  implicit val mediaAtomBeforeCreationFormat: Format[MediaAtomBeforeCreation] =
    Jsonx.formatCaseClass[MediaAtomBeforeCreation]
}

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
    iconikData: Option[IconikData],
    duration: Option[Long],
    source: Option[String],
    description: Option[String],
    trailText: Option[String],
    posterImage: Option[Image],
    trailImage: Option[Image],
    youtubeOverrideImage: Option[Image],
    // metadata
    tags: List[String],
    byline: List[String],
    commissioningDesks: List[String],
    keywords: List[String],
    youtubeCategoryId: Option[String],
    license: Option[String],
    channelId: Option[String],
    legallySensitive: Option[Boolean],
    sensitive: Option[Boolean],
    privacyStatus: Option[PrivacyStatus],
    expiryDate: Option[Long] = None,
    youtubeTitle: String,
    youtubeDescription: Option[String],
    blockAds: Boolean = false,
    composerCommentsEnabled: Option[Boolean] = Some(false),
    optimisedForWeb: Option[Boolean] = Some(false),
    suppressRelatedContent: Option[Boolean] = Some(false),
    videoPlayerFormat: Option[VideoPlayerFormat] = None,
    platform: Option[Platform] = None
) extends MediaAtomBase {

  def asThrift = {
    val data = ThriftMediaAtom(
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
      trailImage = trailImage.map(_.asThrift),
      youtubeOverrideImage = youtubeOverrideImage.map(_.asThrift),
      byline = Some(byline),
      commissioningDesks = Some(commissioningDesks),
      keywords = Some(keywords),
      metadata = Some(
        ThriftMetadata(
          tags = Some(tags),
          categoryId = youtubeCategoryId,
          license = license,
          channelId = channelId,
          privacyStatus = privacyStatus.flatMap(_.asThrift),
          expiryDate = expiryDate,
          pluto = plutoData.map(_.asThrift),
          iconik = iconikData.map(_.asThrift),
          youtube = Some(ThriftYoutubeData(youtubeTitle, youtubeDescription)),
          selfHost =
            videoPlayerFormat.map(vpf => ThriftSelfHostData(Some(vpf.asThrift)))
        )
      ),
      commentsEnabled = composerCommentsEnabled,
      optimisedForWeb = optimisedForWeb,
      suppressRelatedContent = suppressRelatedContent,
      platform = platform.map(_.asThrift)
    )

    ThriftAtom(
      id = id,
      atomType = ThriftAtomType.Media,
      labels = List(),
      defaultHtml = MediaAtomImplicits.defaultMediaHtml(data),
      title = Some(title),
      data = AtomData.Media(data),
      contentChangeDetails = contentChangeDetails.asThrift,
      flags = Some(
        ThriftFlags(
          legallySensitive = legallySensitive,
          blockAds = Some(blockAds),
          sensitive = sensitive
        )
      )
    )
  }

  def getActiveAsset(): Option[Asset] = activeVersion.flatMap(activeVersion =>
    assets.find(_.version == activeVersion)
  )

  def getActiveYouTubeAsset(): Option[Asset] = {
    getActiveAsset() match {
      case Some(asset) if asset.platform == Platform.Youtube => Some(asset)
      case _                                                 => None
    }
  }
}

object MediaAtom extends MediaAtomImplicits {
  implicit val mediaAtomFormat: OFormat[MediaAtom] =
    Jsonx.formatCaseClass[MediaAtom]

  def fromThrift(atom: ThriftAtom) = {
    val data = atom.tdata

    val assets = data.assets.map(Asset.fromThrift).toList
    val activeVersion = data.activeVersion
    val youtubeDescription: Option[String] =
      MediaAtomYoutubeDescriptionHandler.getYoutubeDescription(data)
    val platform = getPlatform(data, activeVersion, assets)

    MediaAtom(
      id = atom.id,
      labels = atom.labels.toList,
      contentChangeDetails =
        ContentChangeDetails.fromThrift(atom.contentChangeDetails),
      assets = assets,
      activeVersion = activeVersion,
      title = data.title,
      category = Category.fromThrift(data.category),
      plutoData = data.metadata.flatMap(_.pluto).map(PlutoData.fromThrift),
      iconikData = data.metadata.flatMap(_.iconik).map(IconikData.fromThrift),
      duration = data.duration,
      source = data.source,
      posterImage = data.posterImage.map(Image.fromThrift),
      trailImage = data.trailImage.map(Image.fromThrift),
      youtubeOverrideImage = data.youtubeOverrideImage.map(Image.fromThrift),
      description = data.description,
      trailText = data.trailText,
      tags = data.metadata.flatMap(_.tags.map(_.toList)).getOrElse(Nil),
      byline = data.byline.map(_.toList).getOrElse(Nil),
      commissioningDesks = data.commissioningDesks.map(_.toList).getOrElse(Nil),
      keywords = data.keywords.map(_.toList).getOrElse(Nil),
      youtubeCategoryId = data.metadata.flatMap(_.categoryId),
      expiryDate = data.metadata.flatMap(_.expiryDate),
      blockAds = atom.flags.flatMap(_.blockAds).getOrElse(false),
      license = data.metadata.flatMap(_.license),
      channelId = data.metadata.flatMap(_.channelId),
      legallySensitive = atom.flags.flatMap(_.legallySensitive),
      sensitive = atom.flags.flatMap(_.sensitive),
      privacyStatus = data.metadata
        .flatMap(_.privacyStatus)
        .flatMap(PrivacyStatus.fromThrift),
      composerCommentsEnabled = data.commentsEnabled,
      optimisedForWeb = data.optimisedForWeb,
      suppressRelatedContent = data.suppressRelatedContent,
      youtubeTitle =
        data.metadata.flatMap(_.youtube).map(_.title).getOrElse(data.title),
      youtubeDescription = youtubeDescription,
      videoPlayerFormat = getVideoPlayerFormat(data.metadata, platform),
      platform = platform
    )
  }

  /** will derive videoPlayerFormat if it is missing
    * @param metadata
    * @param platform
    * @return
    */
  def getVideoPlayerFormat(
      metadata: Option[ThriftMetadata],
      platform: Option[Platform]
  ): Option[VideoPlayerFormat] = {
    metadata
      .flatMap(_.selfHost)
      .flatMap(_.videoPlayerFormat)
      .map(VideoPlayerFormat.fromThrift)
      .orElse(if (platform.contains(Url)) Some(Loop) else None)
  }

  /** will derive atom-level platform if it is missing
    * @param data
    * @param activeVersion
    * @param assets
    * @return
    */
  def getPlatform(
      data: ThriftMediaAtom,
      activeVersion: Option[Long],
      assets: List[Asset]
  ): Option[Platform] = {
    Option(
      Platform.getAtomPlatform(
        data.platform.map(Platform.fromThrift),
        activeVersion
          .flatMap(activeVersion => assets.find(_.version == activeVersion))
          .map(_.platform),
        assets.headOption.map(_.platform)
      )
    )
  }
}
