package model.commands

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult}
import com.gu.media.logging.Logging
import com.gu.media.model.{MediaAtom, PacFileMessage}
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.util.MediaAtomHelpers
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import util.AWSConfig

import java.io.File

case class SubtitleFileUploadCommand(
  mediaAtom: MediaAtom,
  version: Int,
  file: File,
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with Logging {

  override type T = Unit

  override def process(): Unit = {

    val key = s"${awsConfig.userUploadFolder}/${mediaAtom.id}-$version/${file.getName}"

    val metadata = new ObjectMetadata
    metadata.addUserMetadata("user", getUsername(user))

    val request = new PutObjectRequest(awsConfig.userUploadBucket, key, file).withMetadata(metadata)

    awsConfig.s3Client.putObject(request) match {
      case _: PutObjectResult =>
        // ??
      case _ => SubtitleFileUploadFailed
    }
  }
}
