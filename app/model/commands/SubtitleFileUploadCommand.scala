package model.commands

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult}
import com.gu.media.logging.Logging
import com.gu.media.model.{MediaAtom, PacFileMessage, SelfHostedAsset, VideoAsset, VideoSource}
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.upload.model.Upload
import com.gu.media.util.MediaAtomHelpers
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import play.api.libs.Files
import play.api.mvc.MultipartFormData
import util.AWSConfig

import java.io.File

case class SubtitleFileUploadCommand(
  upload: Upload,
  file: MultipartFormData.FilePart[Files.TemporaryFile],
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with Logging {

  override type T = Upload

  override def process(): Upload = {

    val key = s"${awsConfig.userUploadFolder}/${upload.id}/${file.filename}"

    val fileExt = file.filename.split("\\.").last.toLowerCase
    val contentType = fileExt match {
      case "srt" => "application/x-subrip"
      case "vtt" => "text/vtt"
      case _ => "application/octet-stream"
    }

    val metadata = new ObjectMetadata
    metadata.addUserMetadata("user", getUsername(user))
    metadata.setContentType(contentType)

    val request = new PutObjectRequest(awsConfig.userUploadBucket, key, file.ref).withMetadata(metadata)

    awsConfig.s3Client.putObject(request) match {
      case _: PutObjectResult =>
        // add file to upload asset's list of sources
        addSourceToAsset(upload, VideoSource(key, metadata.getContentType))
      case _ => SubtitleFileUploadFailed
    }
  }

  def addSourceToAsset(upload: Upload, source: VideoSource): Upload =
    upload.metadata.asset match {
      case Some(asset: SelfHostedAsset) =>
        val updatedAsset = Some(asset.copy(sources = asset.sources :+ source))
        val updatedMetadata = upload.metadata.copy(asset = updatedAsset)
        upload.copy(metadata = updatedMetadata)
      case _ =>
        upload
  }

}
