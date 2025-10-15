package model.commands

import com.amazonaws.services.s3.model.{
  ObjectMetadata,
  PutObjectRequest,
  PutObjectResult
}
import com.gu.media.logging.Logging
import com.gu.media.model.VideoSource
import com.gu.media.upload.model.Upload
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import play.api.libs.Files
import play.api.mvc.MultipartFormData
import util.{AWSConfig, SubtitleUtil}

/** given an upload record from the dynamo pipeline cache table, this command
  * will add the given file to S3, removing any exising subtitle files. Returns
  * an updated upload record referencing the existing video sources and the new
  * subtitle file.
  *
  * @param upload
  * @param file
  * @param stores
  * @param user
  * @param awsConfig
  */
case class SubtitleFileUploadCommand(
    upload: Upload,
    file: MultipartFormData.FilePart[Files.TemporaryFile],
    override val stores: DataStores,
    user: PandaUser,
    awsConfig: AWSConfig
) extends Command
    with Logging {

  override type T = VideoSource

  override def process(): VideoSource = {

    // remove any existing subtitle files from S3
    SubtitleUtil.deleteSubtitlesFromUserUploadBucket(upload, awsConfig)

    val key = s"${awsConfig.userUploadFolder}/${upload.id}/${file.filename}"

    val metadata = new ObjectMetadata
    metadata.addUserMetadata("user", getUsername(user))
    metadata.setContentType(SubtitleUtil.contentTypeForFilename(file.filename))

    val request =
      new PutObjectRequest(awsConfig.userUploadBucket, key, file.ref)
        .withMetadata(metadata)

    awsConfig.s3Client.putObject(request) match {
      case _: PutObjectResult =>
        VideoSource(key, metadata.getContentType)
      case _ => SubtitleFileUploadFailed
    }
  }
}
