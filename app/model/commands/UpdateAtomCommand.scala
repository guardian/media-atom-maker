package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import model.{ChangeRecord, MediaAtom}

import scala.util.{Failure, Success}
import model.commands.CommandExceptions._
import org.joda.time.DateTime
case class UpdateAtomCommand(id: String, atom: MediaAtom)
                            (implicit previewDataStore: PreviewDataStore,
                             previewPublisher: PreviewAtomPublisher)
    extends Command
    with MediaAtomImplicits {

  type T = MediaAtom

  def process() = {
    if (id != atom.id) {
      AtomIdConflict
    }

    val existingAtom = previewDataStore.getAtom(atom.id)

    if (existingAtom.isEmpty) {
      AtomNotFound
    }

    val changeRecord = ChangeRecord(DateTime.now(), None)

    val details = atom.contentChangeDetails.copy(
      revision = existingAtom.get.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord)
    )
    val thrift = atom.copy(contentChangeDetails = details).asThrift

    previewDataStore.updateAtom(thrift).fold(
      err => AtomUpdateFailed(err.msg),
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => MediaAtom.fromThrift(thrift)
          case Failure(err) => AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
  }
}
