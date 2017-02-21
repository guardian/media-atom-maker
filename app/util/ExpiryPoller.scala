package util

import java.util.Date

import akka.actor.Scheduler
import com.gu.contentatom.thrift.Atom
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTubeVideos
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.Platform.Youtube
import model.{MediaAtom, PrivacyStatus}
import model.commands.{PublishAtomCommand, UpdateAtomCommand}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class ExpiryPoller(dataStores: DataStores, youTube: YouTubeVideos, awsConfig: AWSConfig){
  def start(scheduler: Scheduler): Unit = {
    scheduler.schedule(0.seconds, 6.hours)(checkExpiryDates())
  }

  def checkExpiryDates(): Unit = {
    val timeNow = new Date().getTime
    val user = PandaUser(awsConfig.expiryPollerName, awsConfig.expiryPollerLastName, "expiryPoller", None)

    dataStores.preview.listAtoms.map(atoms => {
      atoms.foreach(atom => {

        ExpiryPoller.updateStatusIfExpired(atom, youTube) match {
          case Some(expiredAtom) => {

            val atomId = expiredAtom.id

            dataStores.published.getAtom(atomId) match {
              case Some(atom) => PublishAtomCommand(atomId, user, dataStores, youTube).process()
              case None => UpdateAtomCommand(expiredAtom.id, expiredAtom, user, dataStores).process()
            }
          }
          case _ =>
        }
      })
    })
  }
}

object ExpiryPoller extends Logging {
  def updateStatusIfExpired(thriftAtom: Atom, youTube: YouTubeVideos): Option[MediaAtom] = {
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

