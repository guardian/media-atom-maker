package model.commands

import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import data.DataStores

case class DeleteCommand(id: String, override val stores: DataStores)
  extends Command with AtomAPIActions with Logging {

  type T = Unit

  override def process(): Unit = {
    val atom = getPreviewAtom(id)

    val event = ContentAtomEvent(atom, EventType.Takedown, new Date().getTime)
    livePublisher.publishAtomEvent(event)
    previewPublisher.publishAtomEvent(event)

    deletePreviewAtom(id)
    deletePublishedAtom(id)
  }
}
