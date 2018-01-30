package model.commands

import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.model.{VideoUpdateError, Asset, MediaAtom}
import com.gu.media.youtube.YouTubeVideos
import data.DataStores
import com.gu.media.model.Platform.Youtube
import model.YouTubeMessage

case class DeleteCommand(id: String, override val stores: DataStores, youTube: YouTubeVideos)
  extends Command with AtomAPIActions with Logging {

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

  private def makeYouTubeVideosPrivate(assets: List[Asset]): Unit = assets.collect {
    case Asset(_, _, videoId, Youtube, _) if youTube.isManagedVideo(videoId) =>
      val privacyStatusUpdate = youTube.setStatus(videoId, PrivacyStatus.Private)

      privacyStatusUpdate match {
        case Right(message: String) => {
          YouTubeMessage(id, videoId, "YouTube Deletion", message).logMessage
        }
        case Left(error: VideoUpdateError) => {
          YouTubeMessage(id, videoId, "YouTube Deletion", error.errorToLog, isError = true).logMessage
        }
      }
  }
}
