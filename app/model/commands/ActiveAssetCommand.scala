package model.commands

import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import com.twitter.util.NonFatal
import data.DataStores
import model.MediaAtom
import model.Platform.Youtube
import model.commands.CommandExceptions._
import util._
import util.atom.MediaAtomImplicits

case class ActiveAssetCommand(atomId: String, params: ActivateAssetRequest, stores: DataStores,
                              youTube: YouTube, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to $params in $atomId")

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    getVersion(mediaAtom) match {
      case Some(version) =>
        val duration = getYouTubeId(version, mediaAtom).flatMap(youTube.getDuration)
        val updatedAtom = mediaAtom.copy(activeVersion = Some(version), duration = duration)

        log.info(s"$params in $atomId")
        UpdateAtomCommand(atomId, updatedAtom, stores, user).process()

      case None =>
        log.info(s"Cannot $params. No asset has that version")
        AssetVersionConflict
    }
  }

  private def getVersion(atom: MediaAtom): Option[Long] = params match {
    case ActivateAssetByVersion(version) =>
      atom.assets.collectFirst {
        case asset if asset.version == version =>
          version
      }

    case ActivateYouTubeAssetById(id) =>
      atom.assets.collectFirst {
        case asset if asset.platform == Youtube && asset.id == id =>
          asset.version
      }
  }

  private def getYouTubeId(version: Long, atom: MediaAtom): Option[String] = {
    atom.assets
      .find(_.version == version)
      .map(_.id)
  }
}
