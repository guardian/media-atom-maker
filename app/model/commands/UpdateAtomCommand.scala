package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import model.{ChangeRecord, MediaAtom}
import data.AuditDataStore

import scala.util.{Failure, Success}
import model.commands.CommandExceptions._
import org.joda.time.DateTime
import ai.x.diff.DiffShow
import ai.x.diff.conversions._

import com.gu.pandomainauth.model.{User => PandaUser}

case class UpdateAtomCommand(id: String, atom: MediaAtom)
                            (implicit previewDataStore: PreviewDataStore,
                             previewPublisher: PreviewAtomPublisher,
                             auditDataStore: AuditDataStore,
                             user: PandaUser)
    extends Command
    with MediaAtomImplicits {

  type T = MediaAtom

  def process(): T = {
    if (id != atom.id) {
      AtomIdConflict
    }

    val oldAtom = previewDataStore.getAtom(atom.id)

    if (oldAtom.isEmpty) {
      AtomNotFound
    }

    val existingAtom = oldAtom.get

    val diffString = auditDataStore.createDiffString(MediaAtom.fromThrift(existingAtom), atom)

    val changeRecord = ChangeRecord(DateTime.now(), None)

    val details = atom.contentChangeDetails.copy(
      revision = existingAtom.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord)
    )
    val thrift = atom.copy(contentChangeDetails = details).asThrift

    previewDataStore.updateAtom(thrift).fold(
      err => AtomUpdateFailed(err.msg),
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => {
            auditDataStore.auditUpdate(id, user, diffString)
            MediaAtom.fromThrift(thrift)
          }
          case Failure(err) => AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
  }
}
