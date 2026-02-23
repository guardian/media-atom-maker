package com.gu.media.model

import com.gu.ai.x.play.json.Encoders._
import com.gu.media.upload.model.Upload
import com.gu.media.youtube.YouTubeProcessingStatus
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

import scala.util.Try

case class ClientAsset(
                        id: String,
                        asset: Option[VideoOutput] = None,
                        processing: Option[ClientAssetProcessing] = None,
                        metadata: Option[ClientAssetMetadata] = None
)
case class ClientAssetProcessing(
    status: String,
    failed: Boolean,
    current: Option[Long],
    total: Option[Long]
)
case class ClientAssetMetadata(
    originalFilename: Option[String],
    subtitleFilename: Option[String],
    startTimestamp: Option[Long],
    user: String
)

object ClientAsset {
  implicit val format: Format[ClientAsset] = Jsonx.formatCaseClass[ClientAsset]

  def fromAssets(assets: List[Asset]): List[ClientAsset] = {
    // Group assets by version and sort to put the newest assets first
    val versions = assets.map(_.version).distinct.sorted.reverse
    val grouped = versions.map { v => assets.filter(_.version == v) }

    grouped.map(videoFromAssets).flatMap { case (version, videoOutputs) =>
      videoOutputs.map(videoOutput => ClientAsset(version.toString, asset = Some(videoOutput)))
    }
  }

  def fromUpload(
      state: String,
      startTimestamp: Long,
      upload: Upload,
      error: Option[String]
  ): ClientAsset = {
    val base = if (upload.metadata.selfHost) {
      selfHostedUpload(state, upload, error)
    } else {
      youTubeUpload(upload, error)
    }

    base.copy(metadata =
      Some(
        ClientAssetMetadata(
          originalFilename = upload.metadata.originalFilename,
          subtitleFilename =
            upload.metadata.subtitleSource.map(VideoInput.filename),
          startTimestamp = Some(startTimestamp),
          user = upload.metadata.user
        )
      )
    )
  }

  def videoFromAssets(assets: List[Asset]): (Long, List[VideoOutput]) = {
    assets.headOption match {
      case Some(Asset(_, version, _, Platform.Url, _, _, _)) =>
        val outputs = assets.map {
          case Asset(_, _, id, _, Some(mimeType), dimensions, _) =>
            SelfHostedOutput(
              id,
              mimeType,
              dimensions.map(_.height),
              dimensions.map(_.width)
            )
        }
        (version, outputs)
      case Some(Asset(_, version, id, Platform.Youtube, _, _, _)) =>
        (version, List(YouTubeOutput(id)))
      case other =>
        throw new IllegalArgumentException(
          s"Unsupported platform ${other.map(_.platform.name)}"
        )
    }
  }

  /** The client asset's id is a string representation of the asset's version
    * within the atom. This method converts the id to a numeric version e.g. to
    * allow sorting
    *
    * @param clientAsset
    * @return
    *   the string id as a numeric version, or zero if it can't be converted
    */
  def getVersion(clientAsset: ClientAsset): Long =
    Try(clientAsset.id.toLong).toOption.getOrElse(0L)

  private def selfHostedUpload(
      state: String,
      upload: Upload,
      error: Option[String]
  ): ClientAsset = {
    ClientAsset(
      id = upload.id,
      asset = None,
      processing = Some(
        ClientAssetProcessing(
          status = error.getOrElse(state),
          failed = error.isDefined,
          current = None,
          total = None
        )
      )
    )
  }

  private def youTubeUpload(
      upload: Upload,
      error: Option[String]
  ): ClientAsset = {
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
  implicit val format: Format[ClientAssetProcessing] =
    Jsonx.formatCaseClass[ClientAssetProcessing]

  def apply(status: YouTubeProcessingStatus): ClientAssetProcessing = {
    ClientAssetProcessing(
      status = getStatusText(status),
      failed = status.failure.nonEmpty,
      current = if (status.processed == 0) { None }
      else { Some(status.processed) },
      total = if (status.total == 0) { None }
      else { Some(status.total) }
    )
  }

  private def getStatusText(status: YouTubeProcessingStatus): String =
    status match {
      case YouTubeProcessingStatus(_, "processing", _, _, 0, _) =>
        "YouTube Processing"

      case YouTubeProcessingStatus(_, "processing", _, _, timeLeftMs, _) =>
        s"YouTube Processing (${timeLeftMs / 1000}s left)"

      case YouTubeProcessingStatus(_, "live", _, _, _, _) =>
        "YouTube Live Stream"

      case _ =>
        status.failure.getOrElse(status.status)
    }
}

object ClientAssetMetadata {
  implicit val format: Format[ClientAssetMetadata] =
    Jsonx.formatCaseClass[ClientAssetMetadata]
}
