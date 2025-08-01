package model.commands

import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, VideoSource}
import com.gu.media.upload.model.Upload
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import util.AWSConfig

case class SubtitleFileDeleteCommand(
  upload: Upload,
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig
) extends Command with Logging {

  override type T = Upload

  override def process(): Upload = {

    val subtitleMimeTypes = Seq("application/x-subrip", "text/vtt")

    val subtitleSource = upload.metadata.asset.flatMap {
      case asset: SelfHostedAsset => asset.sources.find(s => subtitleMimeTypes.contains(s.mimeType))
      case _ => None
    }

    subtitleSource match {
      case Some(source) =>
        // remove subtitle file from s3
        try {
          val request = new DeleteObjectRequest(awsConfig.userUploadBucket, source.src)
          awsConfig.s3Client.deleteObject(request)
        } catch {
          case e: Throwable =>
            log.warn("error deleting subtitle file", e)
        }

        // remove subtitle file from upload asset's list of sources
        removeSourceFromAsset(upload, source)

      case _ =>
        upload
    }
  }

  private def removeSourceFromAsset(upload: Upload, source: VideoSource): Upload =
    upload.metadata.asset match {
      case Some(asset: SelfHostedAsset) =>
        val updatedAsset = Some(asset.copy(sources = asset.sources.filterNot(_ == source)))
        val updatedMetadata = upload.metadata.copy(asset = updatedAsset)
        upload.copy(metadata = updatedMetadata)
      case _ =>
        upload
    }
}
