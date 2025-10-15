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

case class DeleteAssetListCommand(
    atomId: String,
    assets: Seq[Asset],
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
    val assetsVersion = assets.map(_.version).mkString(",")
    val assetsToDelete: Seq[Asset] =
      mediaAtom.assets.filter(asset => assets.exists(_.id == asset.id))

    val markers: LogstashMarker = Markers.appendEntries(
      Map(
        "userId" -> user.email,
        "atomId" -> atomId,
        "assetId" -> assets.map(_.id).mkString(","),
        "assetVersion" -> assetsVersion
      ).asJava
    )

    if (assetsToDelete.nonEmpty) {

      assetsToDelete.foreach { asset =>
        mediaAtom.activeVersion match {
          case Some(activeVersion) if asset.version == activeVersion => {
            log.warn(
              markers,
              s"Cannot delete asset version ${asset.version} on atom $atomId because it is active"
            )
            CannotDeleteActiveAsset
          }
          case _ =>
        }
      }

      val updatedAtom = mediaAtom.copy(
        assets =
          mediaAtom.assets.filterNot(asset => assets.exists(_.id == asset.id))
      )

      UpdateAtomCommand(atomId, updatedAtom, stores, user, awsConfig).process()
    } else {
      log.warn(
        markers,
        s"Asset version ${assetsVersion} does not exist on atom $atomId so cannot be deleted"
      )
      AssetNotFound
    }
  }
}
