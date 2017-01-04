package util

import java.util.Date
import javax.inject.Inject
import akka.actor.Scheduler
import data.AuditDataStore
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}
import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import com.gu.pandomainauth.model.{User => PandaUser}

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
    implicit val username = PandaUser("Expiry", "Poller", "digitalcms.dev@guardian.co.uk", None)

    previewDataStore.listAtoms.map(atoms => {
      atoms.foreach(atom => {

        YouTubeVideoUpdateApi(youtubeConfig).updateStatusIfExpired(atom) match {
          case Some(expiredAtom) => {
            UpdateAtomCommand(expiredAtom.id, expiredAtom).process()

            publishedDataStore.getAtom(expiredAtom.id) match {

              case Some(atom) => PublishAtomCommand(expiredAtom.id).process()
              case _ =>

            }
          }
          case _ =>
        }
      })
    })
  }
}

