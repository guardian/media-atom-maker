package model.commands

import java.util.Date
import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.model.{AdSettings, Asset, MediaAtom, VideoUpdateError}
import data.DataStores
import com.gu.media.model.Platform.Youtube
import model.YouTubeMessage
import util.YouTube

case class DeleteCommand(
    id: String,
    override val stores: DataStores,
    youTube: YouTube
) extends Command
    with Logging {

  type T = Unit

  override def process(): Unit = {
    val atom = getPreviewAtom(id)
    val mediaAtom = MediaAtom.fromThrift(atom)

    makeYouTubeVideosPrivate(mediaAtom.assets)

    val event = ContentAtomEvent(atom, EventType.Takedown, new Date().getTime)
    livePublisher.publishAtomEvent(event)
    previewPublisher.publishAtomEvent(event)

    deletePreviewAtom(id)
    deletePublishedAtom(id)
  }

  private def makeYouTubeVideosPrivate(assets: List[Asset]): Unit =
    assets.collect {
      case Asset(_, _, videoId, Youtube, _, _, _)
          if youTube.isManagedVideo(videoId) =>
        val privacyStatusUpdate =
          youTube.setStatus(videoId, PrivacyStatus.Private)

        privacyStatusUpdate match {
          case Right(message: String) =>
            YouTubeMessage(id, videoId, "Atom Deletion", message).logMessage()

          case Left(error: VideoUpdateError) =>
            YouTubeMessage(
              id,
              videoId,
              "Atom Deletion",
              error.errorToLog,
              isError = true
            ).logMessage()

        }

        youTube.createOrUpdateClaim(id, videoId, AdSettings.NONE) match {
          case Right(message: String) =>
            YouTubeMessage(
              id,
              videoId,
              "Asset marked as private due to atom deletion",
              message
            ).logMessage()
          case Left(error: VideoUpdateError) =>
            YouTubeMessage(
              id,
              videoId,
              "Asset private due to atom Deletion",
              error.errorToLog,
              isError = true
            ).logMessage()
        }
    }
}
