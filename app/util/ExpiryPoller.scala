package util

import java.util.Date
import javax.inject.Inject
import akka.actor.Scheduler
import data.AuditDataStore
import model.MediaAtom
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}
import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import com.gu.pandomainauth.model.{User => PandaUser}

case class ExpiryPoller(previewDataStore: PreviewDataStore,
                        publishedDataStore: PublishedDataStore,
                        previewPublisher: PreviewAtomPublisher,
                        livePublisher: LiveAtomPublisher,
                        youtubeConfig: YouTubeConfig,
                        auditDataStore: AuditDataStore,
                        awsConfig: AWSConfig
                       ){
  def start(scheduler: Scheduler): Unit = {

    scheduler.schedule(0.seconds, 6.hours)(checkExpiryDates())

  }

  def checkExpiryDates(): Unit = {

    val timeNow = new Date().getTime
    val user = PandaUser(awsConfig.expiryPollerName,
      awsConfig.expiryPollerLastName, "expiryPoller", None)

    previewDataStore.listAtoms.map(atoms => {
      atoms.foreach(atom => {

        YouTubeVideoUpdateApi(youtubeConfig).updateStatusIfExpired(atom) match {
          case Some(expiredAtom) => {

            val atomId = expiredAtom.id

            publishedDataStore.getAtom(atomId) match {
              case Some(atom) =>
                PublishAtomCommand(atomId, previewDataStore, previewPublisher, publishedDataStore, livePublisher,
                  auditDataStore, youtubeConfig, user).process()

              case None =>
                UpdateAtomCommand(expiredAtom.id, expiredAtom, previewDataStore, previewPublisher, auditDataStore, user)
                  .process()
            }
          }
          case _ =>
        }
      })
    })
  }
}

