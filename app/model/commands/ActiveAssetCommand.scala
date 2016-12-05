package model.commands

import java.util.Date
import model.MediaAtom

import scala.util.{Failure, Success}
import CommandExceptions._

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{EventType, ContentAtomEvent}
import com.gu.contentatom.thrift.atom.media.Asset
import util.atom.MediaAtomImplicits
import util.{YoutubeResponse, YouTubeConfig, YouTubeVideoInfoApi, SuccesfulYoutubeResponse, YoutubeException}

case class ActiveAssetCommand(atomId: String, youtubeId: String)
                             (implicit previewDataStore: PreviewDataStore,
                              previewPublisher: PreviewAtomPublisher,
                              val youtubeConfig: YouTubeConfig)
  extends Command
  with MediaAtomImplicits {

  type T = MediaAtom




  def getVideoStatus(youtubeId: String): YoutubeResponse = {
    try {
      val status = YouTubeVideoInfoApi(youtubeConfig).getProcessingStatus(youtubeId)
      new SuccesfulYoutubeResponse(status)
    }
    catch {
      case e: Throwable => new YoutubeException(e)
    }
  }
  def process(): MediaAtom = {

    val youtubeResponse = getVideoStatus(youtubeId)

    youtubeResponse match {
      case response: SuccesfulYoutubeResponse => {

        val videoStatus = response.status

        /** Processing status:
        * failed – Video processing has failed.
        * processing – Video is currently being processed.
        * succeeded – Video has been successfully processed.
        * terminated – Processing information is no longer available.
        **/
        videoStatus match {
          case Some(status) if status == "succeeded" || status == "terminated" =>
            previewDataStore.getAtom(atomId) match {
              case Some(atom) =>
                val mediaAtom = atom.tdata
                val atomAssets: Seq[Asset] = mediaAtom.assets

                atomAssets.find(asset => asset.id == youtubeId) match {
                  case Some(newActiveAsset) =>
                    val ytAssetDuration = YouTubeVideoInfoApi(youtubeConfig).getDuration(newActiveAsset.id)

                    val nextAtomRevision = atom
                      .withData(mediaAtom.copy(
                        activeVersion = Some(newActiveAsset.version),
                        duration = ytAssetDuration
                      ))
                      .withRevision(_ + 1)

                    previewDataStore.updateAtom(nextAtomRevision).fold(
                      err => AtomUpdateFailed(err.msg),
                      _ => {
                        val event = ContentAtomEvent(nextAtomRevision, EventType.Update, new Date().getTime)
                        previewPublisher.publishAtomEvent(event) match {
                          case Success(_) => MediaAtom.fromThrift(nextAtomRevision)
                          case Failure(err) => AtomPublishFailed(s"could not publish: ${err.toString}")
                        }
                      }
                    )
                  case None => AssetNotFound(s"could not find asset with id: $youtubeId")
                }
              case None => AtomNotFound
            }
          case Some(_) => AssetEncodingInProcess
          case None => NotYoutubeAsset
        }
      }
      case e: YoutubeException => YoutubeException(e.exception.getMessage)
    }
  }
}
