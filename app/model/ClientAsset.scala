package model

import java.util.UUID

import com.gu.media.model._
import com.gu.media.upload.model.Upload
import com.gu.media.youtube.YouTubeProcessingStatus
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class ClientAsset(id: String, asset: Option[VideoAsset] = None, processing: Option[ClientAssetProcessing] = None, metadata: Option[ClientAssetMetadata] = None)
case class ClientAssetProcessing(status: String, failed: Boolean, current: Option[Long], total: Option[Long])
case class ClientAssetMetadata(originalFilename: Option[String], startTimestamp: Option[Long], user: String)

object ClientAsset {
  implicit val format: Format[ClientAsset] = Jsonx.formatCaseClass[ClientAsset]

  def fromAssets(assets: List[Asset]): List[ClientAsset] = {
    // Group assets by version and sort to put the newest assets first
    val versions = assets.map(_.version).distinct.sorted.reverse
    val grouped = versions.map { v => assets.filter(_.version == v) }

    grouped.map(videoFromAssets).map { case(version, video) =>
      ClientAsset(version.toString, asset = Some(video))
    }
  }

  def fromUpload(state: String, startTimestamp: Long, upload: Upload, error: Option[String]): ClientAsset = {
    val base = if(upload.metadata.selfHost) {
      selfHostedUpload(state, upload, error)
    } else {
      youTubeUpload(upload, error)
    }

    base.copy(metadata = Some(ClientAssetMetadata(
      originalFilename = upload.metadata.originalFilename,
      startTimestamp = Some(startTimestamp),
      user = upload.metadata.user
    )))
  }

  def videoFromAssets(assets: List[Asset]): (Long, VideoAsset) = {
    assets.headOption match {
      case Some(Asset(_, version, _, Platform.Url, _)) =>
        val sources = assets.collect {
          case Asset(_, _, id, _, Some(mimeType)) => VideoSource(id, mimeType)
        }

        (version, SelfHostedAsset(sources))

      case Some(Asset(_, version, id, Platform.Youtube, _)) =>
        (version, YouTubeAsset(id))

      case other =>
        throw new IllegalArgumentException(s"Unsupported platform ${other.map(_.platform.name)}")
    }
  }

  private def selfHostedUpload(state: String, upload: Upload, error: Option[String]): ClientAsset = {
    ClientAsset(
      id = upload.id,
      asset = None,
      processing = Some(ClientAssetProcessing(
        status = error.getOrElse(state),
        failed = error.isDefined,
        current = None,
        total = None
      )
    ))
  }

  private def youTubeUpload(upload: Upload, error: Option[String]): ClientAsset = {
    val processing = error match {
      case Some(msg) =>
        ClientAssetProcessing(
          status = msg,
          failed = true,
          current = None,
          total = None
        )

      case None =>
        val current = upload.progress.chunksInYouTube
        val total = upload.parts.length

        ClientAssetProcessing(
          status = "Uploading to YouTube",
          failed = false,
          current = Some(current),
          total = Some(total)
        )
    }

    ClientAsset(id = upload.id, asset = None, Some(processing), metadata = None)
  }
}

object ClientAssetProcessing {
  implicit val format: Format[ClientAssetProcessing] = Jsonx.formatCaseClass[ClientAssetProcessing]

  def apply(status: YouTubeProcessingStatus): ClientAssetProcessing = {
    ClientAssetProcessing(
      status = getStatusText(status),
      failed = status.failure.nonEmpty,
      current = if(status.processed == 0) { None } else { Some(status.processed) },
      total = if(status.total == 0) { None } else { Some(status.total) }
    )
  }

  private def getStatusText(status: YouTubeProcessingStatus): String = status match {
    case YouTubeProcessingStatus(_, "processing", _, _, 0, _) =>
      "YouTube Processing"

    case YouTubeProcessingStatus(_, "processing", _, _, timeLeftMs, _) =>
      s"YouTube Processing (${timeLeftMs / 1000}s left)"

    case _ =>
      status.failure.getOrElse(status.status)
  }
}

object ClientAssetMetadata {
  implicit val format: Format[ClientAssetMetadata] = Jsonx.formatCaseClass[ClientAssetMetadata]
}
