package data

import com.gu.media.aws.DynamoAccess
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtomPublishSettings
import org.scanamo.generic.auto._
import org.scanamo.syntax._
import org.scanamo.{Scanamo, Table}

case class MediaAtomPublishSettingsStoreException(err: String)
    extends Exception(err)

class MediaAtomPublishSettingsStore(aws: DynamoAccess) extends Logging {
  private val scanamo: Scanamo = aws.scanamo
  private val table: Table[MediaAtomPublishSettings] =
    Table[MediaAtomPublishSettings](aws.mediaAtomPublishSettingsTableName)

  def get(atomId: String): MediaAtomPublishSettings = {
    // no row for this atom yet (e.g. it predates this feature) - default rather than error,
    // to avoid having to backfill every existing atom
    scanamo.exec(table.get("atomId" === atomId)) match {
      case Some(Right(settings)) => settings
      case Some(Left(err)) =>
        log.error(s"Failed to read publish settings for atom $atomId: $err")
        throw MediaAtomPublishSettingsStoreException(err.toString)
      case None => MediaAtomPublishSettings(atomId)
    }
  }

  def put(settings: MediaAtomPublishSettings): Unit = {
    log.info(s"Updating publish settings for atom ${settings.atomId}")
    scanamo.exec(table.put(settings))
  }

  def delete(atomId: String): Unit = {
    log.info(s"Deleting publish settings for atom $atomId")
    scanamo.exec(table.delete("atomId" === atomId))
  }
}
