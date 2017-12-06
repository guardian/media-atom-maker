package model.commands

import java.io.File

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest, PutObjectResult}
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.util.MediaAtomHelpers
import data.DataStores
import com.gu.pandomainauth.model.{User => PandaUser}
import util.AWSConfig
import model.commands.CommandExceptions._
import org.cvogt.play.json.Jsonx
import play.api.libs.json._

case class PacFileUpload(atomId: String, s3Bucket: String, s3Path: String)

object PacFileUpload {
  implicit val format: Format[PacFileUpload] = Jsonx.formatCaseClass[PacFileUpload]

  def build(atom: MediaAtom, putRequest: PutObjectRequest) = {
    PacFileUpload(atom.id, putRequest.getBucketName, putRequest.getKey)
  }
}

case class PacFileUploadCommand (
  mediaAtom: MediaAtom,
  file: File,
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with Logging {

  override type T = PacFileUpload

  override def process(): PacFileUpload = {
    val version = MediaAtomHelpers.getCurrentAssetVersion(mediaAtom).getOrElse(1)
    val key = s"${awsConfig.userUploadFolder}/${mediaAtom.id}-$version/pac.xml"

    val metadata = new ObjectMetadata
    metadata.addUserMetadata("user", getUsername(user))

    val request = new PutObjectRequest(awsConfig.userUploadBucket, key, file).withMetadata(metadata)

    awsConfig.s3Client.putObject(request) match {
      case _: PutObjectResult => {
        val pacFileUpload = PacFileUpload.build(mediaAtom, request)
        awsConfig.sendOnKinesis(awsConfig.uploadsStreamName, key, pacFileUpload)
        pacFileUpload
      }
      case _ => PacFileUploadFailed
    }
  }
}
