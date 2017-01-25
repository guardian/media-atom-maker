package model.commands

import java.util.Date
import model.MediaAtom
import org.joda.time.DateTime

import scala.util.{Failure, Success}
import CommandExceptions._

import play.api.Logger

import data.AuditDataStore
import model.{MediaAtom}
import com.gu.atom.data.{PreviewDataStore, PublishedDataStore}
import com.gu.atom.publish.{PreviewAtomPublisher, LiveAtomPublisher}
import com.gu.contentatom.thrift.atom.media.Asset
import util.atom.MediaAtomImplicits
import util.{YoutubeResponse, YouTubeConfig, YouTubeVideoInfoApi, SuccesfulYoutubeResponse, YoutubeException}

import com.gu.pandomainauth.model.{User => PandaUser}

case class ActiveAssetCommand(atomId: String, youtubeId: String)
                             (implicit previewDataStore: PreviewDataStore,
                              previewPublisher: PreviewAtomPublisher,
                              publishedDataStore: PublishedDataStore,
                              livePublisher: LiveAtomPublisher,
                              val youtubeConfig: YouTubeConfig,
                              auditDataStore: AuditDataStore,
                              user: PandaUser)
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

  def markAssetAsActive(): MediaAtom = {
    previewDataStore.getAtom(atomId) match {
      case Some(atom) =>
        val mediaAtom = atom.tdata
        val atomAssets: Seq[Asset] = mediaAtom.assets

        atomAssets.find(asset => asset.id == youtubeId) match {
          case Some(newActiveAsset) =>

            val ytAssetDuration = YouTubeVideoInfoApi(youtubeConfig).getDuration(newActiveAsset.id)

            val updatedAtom = atom
              .withData(mediaAtom.copy(
                activeVersion = Some(newActiveAsset.version),
                duration = ytAssetDuration
              ))

            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom)).process()

          case None => AssetNotFound
        }
      case None => AtomNotFound
    }
  }


  def process(): T = {
    Logger.info(s"Marking YouTube asset $youtubeId as active")

    getVideoStatus(youtubeId) match {
      case response: SuccesfulYoutubeResponse =>
        val videoStatus = response.status

        /** Processing status:
          * failed – Video processing has failed.
          * processing – Video is currently being processed.
          * succeeded – Video has been successfully processed.
          * terminated – Processing information is no longer available.
          **/
        videoStatus match {
          case Some(status) if status == "succeeded" || status == "terminated" => markAssetAsActive()
          case Some(_) => AssetEncodingInProcess
          case None => NotYoutubeAsset
        }

      case e: YoutubeException => {
        Logger.error(e.toString)
        YouTubeConnectionIssue
      }

    }
  }
}
