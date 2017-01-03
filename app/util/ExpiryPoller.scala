package util

import java.util.Date
import javax.inject.Inject
import akka.actor.Scheduler
import data.AuditDataStore
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}
import play.api.Logger
import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import model.{PrivacyStatus, MediaAtom}
import model.Platform.Youtube


case class ExpiryPoller @Inject () (
                        implicit val previewDataStore: PreviewDataStore,
                        implicit val publishedDataStore: PublishedDataStore,
                        previewPublisher: PreviewAtomPublisher,
                        livePublisher: LiveAtomPublisher,
                        youtubeConfig: YouTubeConfig,
                        implicit val auditDataStore: AuditDataStore
                       ){
  def start(scheduler: Scheduler): Unit = {

    scheduler.schedule(0.seconds, 6.hours)(checkExpiryDates())

  }

  def checkExpiryDates(): Unit = {

    val timeNow = new Date().getTime
    implicit val username = Some("ExpiryPoller")

    previewDataStore.listAtoms.map(atoms => {
      atoms.foreach(thriftAtom => {

        val atom: MediaAtom = MediaAtom.fromThrift(thriftAtom)
        val atomId = atom.id

        atom.expiryDate match {
          case Some(date) => {

            if (date <= timeNow && atom.privacyStatus.get != PrivacyStatus.Private) {

              atom.assets.foreach(asset => {
                if (asset.platform == Youtube) {
                  try {
                    YouTubeVideoUpdateApi(youtubeConfig).setStatusToPrivate(asset.id)
                    val updatedAtom: MediaAtom = atom.copy(privacyStatus=Some(PrivacyStatus.Private))

                    UpdateAtomCommand(atomId, updatedAtom).process()

                    publishedDataStore.getAtom(atomId) match {

                      case Some(atom) => PublishAtomCommand(atomId).process()
                      case _ =>

                    }

                    Logger.info(s"marked video status for video $asset.id in atom $atomId as private")
                  }
                  catch {
                    case e: Throwable => Logger.warn(s"could not mark video status in $asset.id in atom $atomId private $e")
                  }
                }
              })
            }
          }
          case _ =>

        }
      })


    })
  }
}

