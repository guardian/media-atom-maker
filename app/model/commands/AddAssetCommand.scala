package model.commands

import java.net.URI

import com.gu.contentatom.thrift.Atom
import com.gu.contentatom.thrift.atom.media.{Asset, Metadata, Platform, Category => ThriftCategory, MediaAtom => ThriftMediaAtom}
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.util.{MediaAtomImplicits, ThriftUtil}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import com.gu.media.model.MediaAtom.fromThrift
import model.commands.CommandExceptions._
import util.{AWSConfig, YouTube}

case class AddAssetCommand(atomId: String, videoUri: String, override val stores: DataStores,
                           youTube: YouTube, user: PandaUser, awsConfig: AWSConfig)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to add new asset $videoUri to $atomId")

    val atom = getPreviewAtom(atomId)

    val mediaAtom = atom.tdata
    val currentAssets: Seq[Asset] = mediaAtom.assets

    videoUri match {
      case YouTubeId(videoId) if assetAlreadyExists(videoId, currentAssets) =>
        log.info(s"Cannot add asset $videoUri to $atomId as it already exists.")
        AssetAlreadyAdded

      case YouTubeId(videoId) =>
        addYoutubeAsset(atom, mediaAtom, currentAssets, videoId)

      case S3UploaderUri(s3UploaderUri) => addS3UploaderAsset(atom, mediaAtom, currentAssets, s3UploaderUri)

      case _ =>
        NotYoutubeAsset
    }
  }

  private def addAsset(atom: Atom, mediaAtom: ThriftMediaAtom, currentAssets: Seq[Asset], assetId: String, metadata: Option[Metadata]) = {
    val version = getNextAssetVersionNumber(currentAssets)

    val newAsset = ThriftUtil.parseAsset(uri = videoUri, version = version, mimeType = None)
      .fold(_ => AssetParseFailed, identity)

    val updatedMetadata: Metadata = metadata.getOrElse(mediaAtom.metadata.getOrElse(Metadata()))

    val updatedAtom = atom.withData(
      mediaAtom.copy(
        assets = newAsset +: currentAssets,
        metadata = Some(updatedMetadata)
      )
    )

    log.info(s"Adding new asset to atom. atom=$atomId asset=$assetId")

    UpdateAtomCommand(atomId, fromThrift(updatedAtom), stores, user, awsConfig).process()
  }

  private def addYoutubeAsset(atom: Atom, mediaAtom: ThriftMediaAtom, currentAssets: Seq[Asset], videoId: String) = {
    val channel = getYouTubeChannel(videoId, mediaAtom)
    val metadata: Metadata = mediaAtom.metadata.getOrElse(Metadata()).copy(channelId = Some(channel))

    addAsset(atom, mediaAtom, currentAssets, videoId, Some(metadata))
  }

  private def addS3UploaderAsset(atom: Atom, mediaAtom: ThriftMediaAtom, currentAssets: Seq[Asset], s3UploderUri: URI) = {
    addAsset(atom, mediaAtom, currentAssets, s3UploderUri.toString, None)
  }

  private def getYouTubeChannel(videoId: String, atom: ThriftMediaAtom): String = {
    val maybeChannel = atom.metadata.flatMap(_.channelId)
    val maybeVideo = youTube.getVideo(videoId, "snippet")

    (maybeChannel, maybeVideo) match {
      case (_, None) => YouTubeVideoDoesNotExist(videoId)
      case (None, Some(video)) => {
        // only GLabs atoms can have third party videos
        atom.category match {
          case ThriftCategory.Hosted | ThriftCategory.Paid => video.getSnippet.getChannelId
          case _ => NotGLabsAtom
        }
      }
      case (Some(channel), Some(video)) => {
        // new asset must match the atom's channel
        val videoChannel = video.getSnippet.getChannelId
        if (channel == videoChannel) videoChannel else IncorrectYouTubeChannel
      }
    }
  }

  private def getNextAssetVersionNumber (currentAssets: Seq[Asset]): Long = {
    currentAssets.foldLeft(1L){ (acc, asset) => {
      if (asset.version >= acc) asset.version + 1 else acc
    }}
  }

  private def assetAlreadyExists (videoId: String, currentAssets: Seq[Asset]): Boolean = {
    currentAssets.exists(x => x.platform == Platform.Youtube && x.id == videoId)
  }

  private case object YouTubeId {
    def unapply(videoUri: String): Option[String] = {
      val platform = ThriftUtil.parsePlatform(videoUri)

      (platform, videoUri) match {
        case (Right(Platform.Youtube), ThriftUtil.youtube(videoId)) =>
          Some(videoId)

        case _ =>
          None
      }
    }
  }

  // HACK: this is *really* horrible! We should really use the `VideoAsset` case class...
  private case object S3UploaderUri {
    def unapply(url: String): Option[URI] = {
      val platform = ThriftUtil.parsePlatform(url)

      platform match {
        case (Right(Platform.Url)) => {
          try {
            val uri = URI.create(url)
            if (uri.getScheme == "https" && uri.getHost == "uploads.guim.co.uk") Some(uri) else None
          }
          catch {
            case _: Throwable => None
          }
        }
        case _ => None
      }
    }
  }
}
