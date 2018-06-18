package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, MediaAtom}
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import util.{AWSConfig, YouTube}

case class DeleteAssetCommand(
  atomId: String,
  asset: Asset,
  stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig,
  youtube: YouTube
) extends Command with MediaAtomImplicits with Logging {
  type T = MediaAtom

  def process(): T = {
    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val assetsToDelete = mediaAtom.assets.filter(_.id == asset.id)

    if (assetsToDelete.nonEmpty) {
      mediaAtom.activeVersion.foreach(v => {
        if (asset.version == v) {
          log.info("Cannot delete active asset")
          CannotDeleteActiveAsset
        }
      })

      val updatedAtom = mediaAtom.copy(
        assets = mediaAtom.assets.filterNot(_.id == asset.id)
      )

      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig, youtube).process()
    } else {
      AssetNotFound
    }
  }
}
