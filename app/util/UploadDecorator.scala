package util

import com.gu.media.aws.{DynamoAccess, UploadAccess}
import com.gu.media.model.{ClientAsset, ClientAssetMetadata, SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.Upload
import org.scanamo.Table
import org.scanamo.syntax._
import org.scanamo.generic.auto._

class UploadDecorator(aws: DynamoAccess with UploadAccess, stepFunctions: StepFunctions) {
  private val table = Table[Upload](aws.cacheTableName)

  def addMetadataAndSources(atomId: String, video: ClientAsset): ClientAsset = {
    val id = s"$atomId-${video.id}"

    getUpload(id) match {
      case Some(upload) =>
        withMetadataAndSources(video, upload)
      case None =>
        video
    }
  }

  private def getUploadMetadata(upload: Upload): ClientAssetMetadata = {
    ClientAssetMetadata(
      upload.metadata.originalFilename,
      upload.metadata.startTimestamp,
      upload.metadata.user
    )
  }

  private def withMetadataAndSources(video: ClientAsset, upload: Upload): ClientAsset = {
    val updatedMetadata = Some(getUploadMetadata(upload))
    val updatedAsset = video.asset match {
      case Some(selfHostedAsset: SelfHostedAsset) =>
        val updatedSources = selfHostedAsset.sources ++ upload.metadata.subtitleSource
        Some(selfHostedAsset.copy(sources = updatedSources))
      case asset =>
        asset
    }
    video.copy(metadata = updatedMetadata, asset = updatedAsset)
  }

  def getUpload(id: String): Option[Upload] =
    aws.scanamo.exec(table.get("id" === id)).flatMap(_.toOption) orElse { stepFunctions.getById(id) }
}
