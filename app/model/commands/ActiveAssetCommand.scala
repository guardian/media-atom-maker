package model.commands

import com.gu.contentatom.thrift.atom.media.Asset
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import model.commands.CommandExceptions._
import util._

case class ActiveAssetCommand(atomId: String, youtubeId: String, user: PandaUser,
                              stores: DataStores, youTube: YouTubeVideos) extends Command {

  type T = MediaAtom

  def getVideoStatus(youtubeId: String): YoutubeResponse = {
    try {
      val status = youTube.getProcessingStatus(youtubeId)
      new SuccesfulYoutubeResponse(status)
    }
    catch {
      case e: Throwable => new YoutubeException(e)
    }
  }

  def markAssetAsActive(): MediaAtom = {
    stores.preview.getAtom(atomId) match {
      case Some(atom) =>
        val mediaAtom = atom.tdata
        val atomAssets: Seq[Asset] = mediaAtom.assets

        atomAssets.find(asset => asset.id == youtubeId) match {
          case Some(newActiveAsset) =>

            val ytAssetDuration = youTube.getDuration(newActiveAsset.id)

            val updatedAtom = atom
              .withData(mediaAtom.copy(
                activeVersion = Some(newActiveAsset.version),
                duration = ytAssetDuration
              ))

            log.info(s"Marking $youtubeId as the active asset in $atomId")
            UpdateAtomCommand(atomId, MediaAtom.fromThrift(updatedAtom), user, stores).process()

          case None =>
            log.info(s"Cannot mark $youtubeId as the active asset in $atomId. No asset has that id")
            AssetNotFound
        }
      case None =>
        log.info(s"Cannot mark $youtubeId as the active asset in $atomId. No atom has that id")
        AtomNotFound
    }
  }


  def process(): T = {
    log.info(s"Request to mark $youtubeId as the active asset in $atomId")

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
          case Some(status) if status == "succeeded" || status == "terminated" =>
            markAssetAsActive()

          case Some(other) =>
            log.info(s"Cannot mark $youtubeId as the active asset in $atomId. Unexpected processing state $other")
            AssetEncodingInProcess

          case None =>
            log.info(s"Cannot mark $youtubeId as the active asset in $atomId. No youtube video has that id")
            NotYoutubeAsset
        }

      case e: YoutubeException =>
        log.error(s"Cannot mark $youtubeId as the active asset in $atomId. Youtube error", e.exception)
        YouTubeConnectionIssue
    }
  }
}
