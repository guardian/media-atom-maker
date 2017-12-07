package com.gu.media.model

import com.amazonaws.services.s3.model.PutObjectRequest
import com.gu.media.aws.{AwsAccess, UploadAccess}
import com.gu.media.upload.CompleteUploadKey
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

sealed trait PlutoIntegrationData {
  val `type`: String
  val atomId: String
  def partitionKey: String
}

case class PacFileData (
  `type`: String,
  atomId: String,
  s3Bucket: String,
  s3Path: String
) extends PlutoIntegrationData {
  override def partitionKey: String = s3Path
}

object PacFileData {
  implicit val format: Format[PacFileData] = Jsonx.formatCaseClass[PacFileData]

  def build(atom: MediaAtom, putRequest: PutObjectRequest): PacFileData = {
    PacFileData("pac-file-upload", atom.id, putRequest.getBucketName, putRequest.getKey)
  }
}

case class PlutoSyncMetadata (
  `type`: String,
  enabled: Boolean,
  projectId: Option[String],
  s3Key: String,
  atomId: String,
  title: String,
  user: String,
  posterImageUrl: Option[String]
) extends PlutoIntegrationData {
  override def partitionKey: String = s3Key
}

object PlutoSyncMetadata {
  implicit val format: Format[PlutoSyncMetadata] = Jsonx.formatCaseClass[PlutoSyncMetadata]

  def build(uploadId: String, atom: MediaAtom, awsAccess: AwsAccess with UploadAccess, email: String): PlutoSyncMetadata = {
    PlutoSyncMetadata(
      "video-upload",
      awsAccess.syncWithPluto,
      atom.plutoData.flatMap(_.projectId),
      CompleteUploadKey(awsAccess.userUploadFolder, uploadId).toString,
      atom.id,
      atom.title,
      email,
      atom.posterImage.flatMap(_.master).map(_.file)
    )
  }
}

object PlutoIntegrationData {
  implicit val format: Format[PlutoIntegrationData] = Jsonx.formatSealed[PlutoIntegrationData]
}
