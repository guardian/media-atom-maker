package model.commands

import com.gu.media.logging.Logging
import com.gu.media.model.{Asset, MediaAtom}
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import net.logstash.logback.marker.{LogstashMarker, Markers}
import util.AWSConfig
import scala.jdk.CollectionConverters._

case class DeleteAssetCommand(
    atomId: String,
    asset: Asset,
    stores: DataStores,
    user: PandaUser,
    awsConfig: AWSConfig
) extends Command
    with MediaAtomImplicits
    with Logging {
  type T = MediaAtom

  def process(): T = {
    val atom = getPreviewAtom(atomId)
    val mediaAtom = MediaAtom.fromThrift(atom)

    val assetsToDelete: Option[Asset] = mediaAtom.assets.find(_.id == asset.id)

    val markers: LogstashMarker = Markers.appendEntries(
      Map(
        "userId" -> user.email,
        "atomId" -> atomId,
        "assetId" -> asset.id,
        "assetVersion" -> asset.version
      ).asJava
    )

    if (assetsToDelete.nonEmpty) {
      mediaAtom.activeVersion match {
        case Some(activeVersion)
            if assetsToDelete.get.version == activeVersion => {
          log.warn(
            markers,
            s"Cannot delete asset version ${asset.version} on atom $atomId because it is active"
          )
          CannotDeleteActiveAsset
        }
        case _ =>
      }

      val updatedAtom = mediaAtom.copy(
        assets = mediaAtom.assets.filterNot(_.id == asset.id)
      )

      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig).process()
    } else {
      log.warn(
        markers,
        s"Asset version ${asset.version} does not exist on atom $atomId so cannot be deleted"
      )
      AssetNotFound
    }
  }
}
