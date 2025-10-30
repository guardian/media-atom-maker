package com.gu.media.util

import com.gu.contentatom.thrift.{Atom, AtomData, ChangeRecord, User}
import com.gu.contentatom.thrift.atom.media.{
  Asset => ThriftAsset,
  AssetType,
  Platform => ThriftPlatform,
  MediaAtom => ThriftMediaAtom
}
import com.gu.contentatom.thrift.{
  ImageAssetDimensions => ThriftImageAssetDimensions
}
import com.gu.media.model._
import org.joda.time.DateTime

import java.net.URLEncoder

object MediaAtomHelpers {
  def updateAtom(atom: Atom, user: User)(
      fn: ThriftMediaAtom => ThriftMediaAtom
  ): Atom = {
    val before = atom.data.asInstanceOf[AtomData.Media].media
    val after = fn(before)

    atom.copy(
      data = AtomData.Media(after),
      contentChangeDetails = atom.contentChangeDetails.copy(
        lastModified = Some(ChangeRecord(DateTime.now().getMillis, Some(user))),
        revision = atom.contentChangeDetails.revision + 1
      )
    )
  }

  def getCurrentAssetVersion(mediaAtom: MediaAtom): Option[Long] = {
    if (mediaAtom.assets.isEmpty) {
      None
    } else {
      Some(mediaAtom.assets.map(_.version).max)
    }
  }

  def findSelfHostedAsset(
      mediaAtom: MediaAtom,
      mimeType: String,
      version: Long
  ): Option[Asset] =
    mediaAtom.assets.find(asset =>
      asset.platform == Platform.Url &&
        asset.mimeType.contains(mimeType) &&
        asset.version == version
    )

  def findActiveSelfHostedAsset(
      mediaAtom: MediaAtom,
      mimeType: String
  ): Option[Asset] =
    mediaAtom.activeVersion.flatMap(ver =>
      findSelfHostedAsset(mediaAtom, mimeType, ver)
    )

  def getCurrentAssetVersion(mediaAtom: ThriftMediaAtom): Option[Long] = {
    if (mediaAtom.assets.isEmpty) {
      None
    } else {
      Some(mediaAtom.assets.map(_.version).max)
    }
  }

  def getNextAssetVersion(mediaAtom: ThriftMediaAtom): Long = {
    getCurrentAssetVersion(mediaAtom).getOrElse(0L) + 1
  }

  def addAsset(
      mediaAtom: ThriftMediaAtom,
      asset: VideoAsset,
      version: Long
  ): ThriftMediaAtom = {
    val assets = getAssets(asset, version)

    // remove any existing assets that have the same version
    val atomAssets = mediaAtom.assets.filter(a => a.version != version)
    val updatedAssets = assets ++ atomAssets

    mediaAtom.copy(assets = updatedAssets)
  }

  /** Tries to create a meaningful User object by inferring a user's name based
    * on an email address, specifically a guardian email, for saving changes to
    * atoms where no name is explicitly available.
    *
    * Covers common patterns such as joe.bloggs@guardian.co.uk,
    * joe.bloggs.contractor@guardian.co.uk, but falls back to blank names if no
    * match found.
    *
    * @param email
    * @return
    */
  def getUser(email: String): User = {
    email match {
      case s"$firstname.$lastname.$_@$_" =>
        // first.last.freelancer@guardian.co.uk
        User(email, Some(firstname.capitalize), Some(lastname.capitalize))
      case s"$firstname.$lastname@$_" =>
        // first.last@guardian.co.uk
        User(email, Some(firstname.capitalize), Some(lastname.capitalize))
      case _ =>
        // can't extract a name
        User(email)
    }
  }

  /** Takes a SelfHostedAsset that has sources as relative s3 keys and rewrites
    * the sources as full urls.
    *
    * Where forward slashes are detected in the relative key, the key is split
    * into path and filename and only the filename is url encoded, so that the
    * slashes are preserved. This is to satisfy the m3u8 transcoder, which uses
    * relative paths in its manifest. e.g. 2025/08/18/My
    * Title--0653ffba-35f4-4883-b961-3139cdaf6c8b-1.0.m3u8 becomes:
    * https://gu.com/videos/2025/08/18/My%20Title--0653ffba-35f4-4883-b961-3139cdaf6c8b-1.0.m3u8
    *
    * @param selfHostedAsset
    *   \- self-hosted asset with sources like "some/path/some-file"
    * @param selfHostedOrigin
    *   \- the prefix for the urls e.g. https://gu.com/videos
    * @return
    *   the self-hosted asset with url-encoded sources
    */
  def urlEncodeSources(
      selfHostedAsset: SelfHostedAsset,
      selfHostedOrigin: String
  ): SelfHostedAsset = {
    SelfHostedAsset(selfHostedAsset.sources.map { source =>
      val parts = source.src.split("/")
      parts.length match {
        case 1 =>
          source.copy(src =
            s"$selfHostedOrigin/${URLEncoder.encode(source.src, "UTF-8")}"
          )
        case _ =>
          val filename = parts.last
          val path = parts.dropRight(1).mkString("/")
          source.copy(src =
            s"$selfHostedOrigin/$path/${URLEncoder.encode(filename, "UTF-8")}"
          )
      }
    })
  }

  private def getAssets(asset: VideoAsset, version: Long): List[ThriftAsset] =
    asset match {
      case YouTubeAsset(id) =>
        val asset = ThriftAsset(
          AssetType.Video,
          version,
          id,
          ThriftPlatform.Youtube,
          mimeType = None
        )

        List(asset)

      case SelfHostedAsset(sources) =>
        val assets = sources.map {
          case VideoSource(src, mimeType, height, width) =>
            val (dimensions, aspectRatio) = (height, width) match {
              case (Some(h), Some(w)) =>
                Some(ThriftImageAssetDimensions(h, w)) ->
                  AspectRatio.calculate(w, h)
              case _ =>
                None -> None
            }
            ThriftAsset(
              AssetType.Video,
              version,
              src,
              ThriftPlatform.Url,
              Some(mimeType),
              dimensions,
              aspectRatio.map(_.name)
            )
        }

        val subtitleAssets = sources.collect {
          case VideoSource(src, VideoSource.mimeTypeM3u8, _, _) =>
            val subtitleSrc = src.dropRight(5) + VideoSource.captionsSuffix
            ThriftAsset(
              AssetType.Subtitles,
              version,
              subtitleSrc,
              ThriftPlatform.Url,
              Some(VideoSource.mimeTypeVtt)
            )
        }

        assets ++ subtitleAssets
    }
}
