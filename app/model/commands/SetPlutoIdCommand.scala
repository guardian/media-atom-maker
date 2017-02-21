package model.commands

import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.MediaAtom
import model.commands.CommandExceptions.AtomNotFound

class SetPlutoIdCommand(atomId: String, plutoId: String, user: PandaUser, stores: DataStores) extends Command {
  override type T = MediaAtom

  override def process(): MediaAtom = {
    log.info(s"Request received to set pluto id $plutoId for atom $atomId")

    stores.preview.getAtom(atomId) match {
      case Some(before) =>
        val after = before.updateData { data =>
          data.copy(plutoProjectId = Some(plutoId))
        }

        UpdateAtomCommand(atomId, MediaAtom.fromThrift(after), user, stores).process()

      case None =>
        log.info(s"Cannot set pluto id $plutoId for atom $atomId. No atom has that id")
        AtomNotFound
    }
  }
}
