package model.commands

import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.{Audit, MediaAtom}
import util.atom.MediaAtomImplicits

class SetPlutoIdCommand(atomId: String, plutoId: String, override val stores: DataStores, user: PandaUser)

  extends Command with MediaAtomImplicits with Logging {

  override type T = MediaAtom

  override def process(): (MediaAtom, Audit) = {
    log.info(s"Request received to set pluto id $plutoId for atom $atomId")

    val before = getPreviewAtom(atomId)

    val after = before.updateData { data =>
      data.copy(plutoProjectId = Some(plutoId))
    }

    UpdateAtomCommand(atomId, MediaAtom.fromThrift(after), stores, user).process()
  }
}
