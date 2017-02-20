package model.commands

import java.util.Date

import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{ChangeRecord, MediaAtom}

import scala.util.{Failure, Success}

case class UpdateAtomCommand(id: String, atom: MediaAtom, user: PandaUser, stores: DataStores) extends Command {
  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to update atom ${atom.id}")

    if (id != atom.id) {
      AtomIdConflict
    }

    val oldAtom = stores.preview.getAtom(atom.id)

    if (oldAtom.isEmpty) {
      log.info(s"Unable to update atom ${atom.id}. Atom does not exist")
      AtomNotFound
    }

    val existingAtom = oldAtom.get

    val diffString = stores.audit.createDiffString(MediaAtom.fromThrift(existingAtom), atom)
    log.info(s"Update atom changes ${atom.id}: $diffString")

    val changeRecord = ChangeRecord.now(user)

    val details = atom.contentChangeDetails.copy(
      revision = existingAtom.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord)
    )
    val thrift = atom.copy(contentChangeDetails = details).asThrift

    stores.preview.updateAtom(thrift).fold(
      err => {
        log.error(s"Unable to update atom ${atom.id}", err)
        AtomUpdateFailed(err.msg)
      },
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        stores.previewPublisher.publishAtomEvent(event) match {
          case Success(_) => {
            stores.audit.auditUpdate(id, user, diffString)

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
}
