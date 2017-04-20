package model.commands

import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.AuditEvent

case class DeleteCommand(id: String, user: PandaUser, override val stores: DataStores)
  extends Command with AtomAPIActions with Logging {

  type T = Unit

  override def process(): (Unit, AuditEvent) = {
    val atom = getPreviewAtom(id)

    val capiEvent = ContentAtomEvent(atom, EventType.Takedown, new Date().getTime)
    livePublisher.publishAtomEvent(capiEvent)
    previewPublisher.publishAtomEvent(capiEvent)

    deletePreviewAtom(id)
    deletePublishedAtom(id)

    val auditEvent = AuditEvent.delete(user, id)
    ((), auditEvent)
  }
}
