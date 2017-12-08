package model.commands

import java.io.File

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult}
import com.gu.media.logging.Logging
import com.gu.media.model.{MediaAtom, PacFileMessage}
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.util.MediaAtomHelpers
import data.DataStores
import com.gu.pandomainauth.model.{User => PandaUser}
import util.AWSConfig
import model.commands.CommandExceptions._

case class PacFileUploadCommand (
  mediaAtom: MediaAtom,
  file: File,
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with Logging {

  override type T = PacFileMessage

  override def process(): PacFileMessage = {
    val version = MediaAtomHelpers.getCurrentAssetVersion(mediaAtom).getOrElse(1)
    val key = s"${awsConfig.userUploadFolder}/${mediaAtom.id}-$version/pac.xml"

    val metadata = new ObjectMetadata
    metadata.addUserMetadata("user", getUsername(user))

    val request = new PutObjectRequest(awsConfig.userUploadBucket, key, file).withMetadata(metadata)

    awsConfig.s3Client.putObject(request) match {
      case _: PutObjectResult => {
        val pacFileUpload = PacFileMessage.build(mediaAtom, request)

        val plutoActions = new PlutoUploadActions(awsConfig)
        plutoActions.sendToPluto(pacFileUpload)

        pacFileUpload
      }
      case _ => PacFileUploadFailed
    }
  }
}
