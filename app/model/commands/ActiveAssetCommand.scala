package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.model.Platform.Youtube
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import util._

case class ActiveAssetCommand(
  atomId: String,
  activateAssetRequest: ActivateAssetRequest,
  stores: DataStores,
  youtube: YouTube,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with MediaAtomImplicits with Logging {

  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to set active asset atom=$atomId version=${activateAssetRequest.version}")

    if (atomId != activateAssetRequest.atomId) {
      AtomIdConflict
    }

    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val assetsToActivate = mediaAtom.assets.filter(_.version == activateAssetRequest.version)

    if (assetsToActivate.nonEmpty) {
      // Attempt to get atom duration from YouTube otherwise use the current value
      val duration = assetsToActivate.find(_.platform == Youtube)
        .flatMap(asset => youtube.getDuration(asset.id))
        .orElse(mediaAtom.duration)

      val updatedAtom = mediaAtom.copy(
        activeVersion = Some(activateAssetRequest.version),
        duration = duration
      )

      log.info(s"Setting active asset atom=$atomId version=${activateAssetRequest.version}")
      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig).process()
    } else {
      log.info(s"Cannot set active asset. No asset has that version atom=$atomId version=${activateAssetRequest.version}")
      AssetVersionConflict
    }
  }
}
