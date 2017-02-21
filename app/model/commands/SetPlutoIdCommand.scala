package model.commands

import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import model.commands.CommandExceptions.AtomNotFound
import util.atom.MediaAtomImplicits

class SetPlutoIdCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser)

  extends Command with MediaAtomImplicits with Logging {

  override type T = MediaAtom

  override def process(): MediaAtom = {
    log.info(s"Request received to set pluto id $plutoId for atom $atomId")

    previewDataStore.getAtom(atomId) match {
      case Some(before) =>
        val after = before.updateData { data =>
          data.copy(plutoProjectId = Some(plutoId))
        }

        UpdateAtomCommand(atomId, MediaAtom.fromThrift(after), stores, user).process()

      case None =>
        log.info(s"Cannot set pluto id $plutoId for atom $atomId. No atom has that id")
        AtomNotFound
    }
  }
}
