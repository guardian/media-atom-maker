package util

import java.util.Date

import akka.actor.Scheduler
import com.gu.pandomainauth.model.{User => PandaUser}
import data.{DataStores, HasDataStores}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class ExpiryPoller(override val stores: DataStores, youtubeConfig: YouTubeConfig, awsConfig: AWSConfig) extends HasDataStores {

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
                PublishAtomCommand(atomId, stores, youtubeConfig, user).process()

              case None =>
                UpdateAtomCommand(expiredAtom.id, expiredAtom, stores, user).process()
            }
          }
          case _ =>
        }
      })
    })
  }
}

