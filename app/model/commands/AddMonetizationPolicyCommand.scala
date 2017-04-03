package model.commands

import com.gu.atom.play.AtomAPIActions
import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTubeClaims
import data.DataStores
import model.MediaAtom


class AddMonetizationPolicyCommand (id: String, override val stores: DataStores, claims: YouTubeClaims)
  extends Command with AtomAPIActions with Logging {

  type T = Unit

  def process(): Unit = {
    log.info(s"Request to set usage policy on atom $id")

    val thriftAtom = getPreviewAtom(id)
    val atom = MediaAtom.fromThrift(thriftAtom)

    MediaAtom.getActiveYouTubeAsset(atom) match {
      case Some(youTubeAsset) => {
        val youTubeId = youTubeAsset.id
        claims.setVideoClaim(atom.title, atom.description.get, atom.addsTurnedOff, youTubeId)
      }
      case None => {
        log.info(s"Atom $id does not have an active youtube video: usage policy was not set")
      }
    }
  }
 }
