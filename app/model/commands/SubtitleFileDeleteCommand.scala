package model.commands

import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import util.{AWSConfig, SubtitleUtil}

/** given an upload record from the dynamo pipeline cache table, this command
  * will remove any referenced subtitle files from S3 and the upload record.
  * Returns an updated upload record containing references to the existing video
  * sources.
  *
  * @param upload
  * @param stores
  * @param user
  * @param awsConfig
  */
case class SubtitleFileDeleteCommand(
    upload: Upload,
    override val stores: DataStores,
    user: PandaUser,
    awsConfig: AWSConfig
) extends Command
    with Logging {

  override type T = Unit

  override def process(): Unit = {
    // remove subtitle files from S3
    SubtitleUtil.deleteSubtitlesFromUserUploadBucket(upload, awsConfig)
  }
}
