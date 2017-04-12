package model.commands

import java.util.Date

import com.gu.atom.play.AtomAPIActions
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.media.AuditEvents
import com.gu.media.logging.Logging
import data.DataStores
import model.Audit

case class DeleteCommand(id: String, user: PandaUser, override val stores: DataStores)
  extends Command with AtomAPIActions with Logging {

  type T = Unit

  override def process(): (Unit, Audit) = {
    val atom = getPreviewAtom(id)

    val event = ContentAtomEvent(atom, EventType.Takedown, new Date().getTime)
    livePublisher.publishAtomEvent(event)
    previewPublisher.publishAtomEvent(event)

    deletePreviewAtom(id)
    deletePublishedAtom(id)

    ((), Audit(id, AuditEvents.DELETE, "Atom deleted", user))
  }
}
