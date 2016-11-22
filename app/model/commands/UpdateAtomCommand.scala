package model.commands

import java.util.Date

import com.gu.atom.data.PreviewDataStore
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import model.commands.CommandExceptions._
import util.atom.MediaAtomImplicits
import model.MediaAtom

import scala.util.{Failure, Success}

import model.commands.CommandExceptions._
case class UpdateAtomCommand(id: String, atom: MediaAtom)
                            (implicit previewDataStore: PreviewDataStore,
                             previewPublisher: PreviewAtomPublisher)
    extends Command
    with MediaAtomImplicits {

  type T = Unit

  def process(): Unit = {
    if (id != atom.id) {
      AtomIdConflict
    }
    val details = atom.contentChangeDetails.copy(revision = atom.contentChangeDetails.revision + 1)
    val thrift = atom.copy(contentChangeDetails = details).asThrift

    previewDataStore.updateAtom(thrift).fold(
      err => AtomUpdateFailed(err.msg),
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => ()
          case Failure(err) => AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
  }
}
