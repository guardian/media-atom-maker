package com.gu.media.model

import com.amazonaws.services.s3.model.PutObjectRequest
import com.gu.media.aws.{AwsAccess, UploadAccess}
import com.gu.media.upload.CompleteUploadKey
import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format

sealed trait PlutoIntegrationMessage {
  val `type`: String
  val atomId: String
  def partitionKey: String
}

case class AtomAssignedProjectMessage(
    `type`: String,
    atomId: String,
    commissionId: String,
    projectId: String,
    title: String,
    user: Option[String]
) extends PlutoIntegrationMessage {
  override def partitionKey: String = atomId
}

object AtomAssignedProjectMessage {
  implicit val format: Format[AtomAssignedProjectMessage] =
    Jsonx.formatCaseClass[AtomAssignedProjectMessage]

  def build(atom: MediaAtom): AtomAssignedProjectMessage = {
    val plutoData = atom.plutoData.get

    val email = for {
      created <- atom.contentChangeDetails.created
      user <- created.user
    } yield user.email

    AtomAssignedProjectMessage(
      "project-assigned",
      atom.id,
      plutoData.commissionId.get,
      plutoData.projectId.get,
      atom.title,
      email
    )
  }
}

case class PacFileMessage(
    `type`: String,
    atomId: String,
    s3Bucket: String,
    s3Path: String
) extends PlutoIntegrationMessage {
  override def partitionKey: String = s3Path
}

object PacFileMessage {
  implicit val format: Format[PacFileMessage] =
    Jsonx.formatCaseClass[PacFileMessage]

  def build(atom: MediaAtom, putRequest: PutObjectRequest): PacFileMessage = {
    PacFileMessage(
      "pac-file-upload",
      atom.id,
      putRequest.getBucketName,
      putRequest.getKey
    )
  }
}

case class PlutoSyncMetadataMessage(
    `type`: String,
    projectId: Option[String],
    s3Key: String,
    atomId: String,
    title: String,
    user: String,
    posterImageUrl: Option[String]
) extends PlutoIntegrationMessage {
  override def partitionKey: String = s3Key
}

object PlutoSyncMetadataMessage {
  implicit val format: Format[PlutoSyncMetadataMessage] =
    Jsonx.formatCaseClass[PlutoSyncMetadataMessage]

  def build(
      uploadId: String,
      atom: MediaAtom,
      awsAccess: AwsAccess with UploadAccess,
      email: String
  ): PlutoSyncMetadataMessage = {
    PlutoSyncMetadataMessage(
      "video-upload",
      atom.plutoData.flatMap(_.projectId),
      CompleteUploadKey(awsAccess.userUploadFolder, uploadId).toString,
      atom.id,
      atom.title,
      email,
      atom.posterImage.flatMap(_.master).map(_.file)
    )
  }
}

case class PlutoResyncMetadataMessage(
    `type`: String,
    projectId: Option[String],
    s3Key: String,
    atomId: String,
    title: String,
    posterImageUrl: Option[String]
) extends PlutoIntegrationMessage {
  override def partitionKey: String = s3Key
}

object PlutoResyncMetadataMessage {
  implicit val format: Format[PlutoResyncMetadataMessage] =
    Jsonx.formatCaseClass[PlutoResyncMetadataMessage]

  def build(
      uploadId: String,
      atom: MediaAtom,
      awsAccess: AwsAccess with UploadAccess
  ): PlutoResyncMetadataMessage = {
    PlutoResyncMetadataMessage(
      "video-upload-resync",
      atom.plutoData.flatMap(_.projectId),
      CompleteUploadKey(awsAccess.userUploadFolder, uploadId).toString,
      atom.id,
      atom.title,
      atom.posterImage.flatMap(_.master).map(_.file)
    )
  }
}

object PlutoIntegrationMessage {
  implicit val format: Format[PlutoIntegrationMessage] =
    Jsonx.formatSealed[PlutoIntegrationMessage]
}
