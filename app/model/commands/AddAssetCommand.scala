package model.commands

import com.gu.media.logging.Logging
import com.gu.media.youtube.YouTube
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Asset, MediaAtom}
import util.atom.MediaAtomImplicits

case class AddAssetCommand(atomId: String, newAssets: List[Asset], override val stores: DataStores,
                           youTube: YouTube, user: PandaUser)
  extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    log.info(s"Request to add new asset ${newAssets.mkString(",")} to $atomId")

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)
    val currentAssets: Seq[Asset] = mediaAtom.assets

    val nextVersion = currentAssets.map(_.version).max + 1
    val versionedAssets = newAssets.map(_.copy(version = nextVersion))

    versionedAssets.foreach { asset =>
      checkNotAlreadyAdded(currentAssets, asset)
    }

    val updatedAtom = mediaAtom.copy(assets = versionedAssets ++ currentAssets)

    log.info(s"Adding new asset ${newAssets.mkString(",")} to $atomId")
    UpdateAtomCommand(atomId, updatedAtom, stores, user).process()
  }

  private def checkNotAlreadyAdded(current: Seq[Asset], asset: Asset) = {
    current.find(_.id == asset.id).foreach { _ =>
      log.info(s"${asset.id} has already been added to $atomId")
      AssetVersionConflict
    }
  }
}
