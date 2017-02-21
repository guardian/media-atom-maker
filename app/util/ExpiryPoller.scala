package util

import java.util.Date

import akka.actor.Scheduler
import com.gu.contentatom.thrift.Atom
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.{DataStores, HasDataStores}
import model.Platform.Youtube
import model.{MediaAtom, PrivacyStatus}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class ExpiryPoller(override val stores: DataStores, youTube: YouTube, awsConfig: AWSConfig)
  extends HasDataStores with Logging {

  def start(scheduler: Scheduler): Unit = {
    scheduler.schedule(0.seconds, 6.hours)(checkExpiryDates())
  }

  def checkExpiryDates(): Unit = {

    val timeNow = new Date().getTime
    val user = PandaUser(awsConfig.expiryPollerName,
      awsConfig.expiryPollerLastName, "expiryPoller", None)

    previewDataStore.listAtoms.map(atoms => {
      atoms.foreach(atom => {

        updateStatusIfExpired(atom) match {
          case Some(expiredAtom) => {

            val atomId = expiredAtom.id

            publishedDataStore.getAtom(atomId) match {
              case Some(atom) =>
                PublishAtomCommand(atomId, stores, youTube, user).process()

              case None =>
                UpdateAtomCommand(expiredAtom.id, expiredAtom, stores, user).process()
            }
          }
          case _ =>
        }
      })
    })
  }

  def updateStatusIfExpired(thriftAtom: Atom): Option[MediaAtom] = {

    val atom: MediaAtom = MediaAtom.fromThrift(thriftAtom)
    val atomId = atom.id
    val timeNow = new Date().getTime

    atom.expiryDate match {
      case Some(date) => {

        if (date <= timeNow && atom.privacyStatus.get != PrivacyStatus.Private) {
          atom.assets.collect {
            case asset if asset.platform == Youtube =>
              log.info(s"Marking asset=${asset.id} atom=$atomId as private due to expiry")
              youTube.setStatusToPrivate(asset.id, atomId)
          }

          val updatedAtom = atom.copy(privacyStatus = Some(PrivacyStatus.Private))
          Some(updatedAtom)

        } else None

      }
      case _ => None
    }
  }
}

