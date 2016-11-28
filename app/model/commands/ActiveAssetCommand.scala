package model.commands

import java.util.Date
import scala.util.{Failure, Success}
import CommandExceptions._

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{EventType, ContentAtomEvent}
import com.gu.contentatom.thrift.atom.media.Asset
import util.atom.MediaAtomImplicits
import util.{YoutubeResponse, YouTubeConfig, YouTubeVideoStatusApi, SuccesfulYoutubeResponse, YoutubeException}

case class ActiveAssetCommand(atomId: String, youtubeId: String)
                             (implicit previewDataStore: PreviewDataStore,
                              previewPublisher: PreviewAtomPublisher,
                              val youtubeConfig: YouTubeConfig)
  extends Command
  with MediaAtomImplicits {

  type T = Unit




  def getVideoStatus(youtubeId: String): YoutubeResponse = {
    try {
      val status = YouTubeVideoStatusApi(youtubeConfig).get(youtubeId)
      new SuccesfulYoutubeResponse(status)
    }
    catch {
      case e: Throwable => new YoutubeException(e)
    }
  }
  def process(): Unit = {

    val youtubeResponse = getVideoStatus(youtubeId)

    youtubeResponse match {
      case response: SuccesfulYoutubeResponse => {

        val videoStatus = response.status
        videoStatus match {
          case Some("succeeded") => {

            previewDataStore.getAtom(atomId) match {
              case Some(atom) => {
                val mediaAtom = atom.tdata
                val atomAssets: Seq[Asset] = mediaAtom.assets

                atomAssets.find(asset => asset.id == youtubeId) match {
                  case Some(newActiveAsset) => {

                    val nextAtomRevision = atom
                      .withData(mediaAtom.copy(
                        activeVersion = Some(newActiveAsset.version)
                      ))
                      .withRevision(_ + 1)

                    previewDataStore.updateAtom(nextAtomRevision).fold(
                      err => InternalServerError(err.msg),
                      _ => {
                        val event = ContentAtomEvent(nextAtomRevision, EventType.Update, new Date().getTime)

                        previewPublisher.publishAtomEvent(event) match {
                          case Success(_) => ()
                          case Failure(err) => InternalServerError(s"could not publish: ${err.toString}")
                        }
                      }
                    )
                  }
                  case None => BadRequest(s"could not find asset with id: ${youtubeId}")
                }
              }
              case None => AtomNotFound
            }
          }
          case Some(_) => AssetEncodingInProcess
          case None => NotYoutubeAsset
        }
      }
      case e: YoutubeException => BadRequest(e.toString())
    }
  }
}
