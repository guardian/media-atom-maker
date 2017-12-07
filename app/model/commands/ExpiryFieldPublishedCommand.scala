package model.commands

import java.util.Date

import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.media.model.{ChangeRecord, MediaAtom}
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import org.joda.time.DateTime

import scala.util.{Failure, Success}

case class ExpiryFieldPublishedCommand(override val stores: DataStores, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = List[MediaAtom]


  def process(): T = {
    publishedDataStore.listAtoms.fold(
      {
        err => AtomUpdateFailed(err.msg)
      },

      {
        atoms => atoms.map(atom => reIndex(MediaAtom.fromThrift(atom)))
      }
    )
  }

  def reIndex(atom: MediaAtom) = {
    val expiry: Option[DateTime] = atom.expiryDate.map(expiry => new DateTime(expiry))

    expiry match {
      case Some(_) => {
        log.info(s"Request to update atom ${atom.id}")

        val details = atom.contentChangeDetails.copy(
          expiry = expiry.map(ChangeRecord.build(_, user)),
          revision = atom.contentChangeDetails.revision + 1
        )
        val thrift = atom.copy(contentChangeDetails = details).asThrift


        publishedDataStore.updateAtom(thrift).fold(
          err => {
            log.error(s"Unable to update atom ${atom.id}", err)
            AtomUpdateFailed(err.msg)
          },
          _ => {
            val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

            livePublisher.publishAtomEvent(event) match {
              case Success(_) => {
                log.info(s"Successfully updated atom ${atom.id}")
                MediaAtom.fromThrift(thrift)
              }
              case Failure(err) =>
                log.error(s"Unable to publish updated atom ${atom.id}", err)
                AtomPublishFailed(s"could not publish: ${err.toString}")
            }
          }
        )
      }
      case None => {
        atom
      }
    }
  }
}
