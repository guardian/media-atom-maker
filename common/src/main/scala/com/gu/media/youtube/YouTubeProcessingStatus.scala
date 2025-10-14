package com.gu.media.youtube

import com.google.api.services.youtube.model.{Video, VideoProcessingDetails}
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format

import scala.util.Try

// failure only set is status is "failed"
case class YouTubeProcessingStatus(
    id: String,
    status: String,
    total: Long,
    processed: Long,
    timeLeftMs: Long,
    failure: Option[String]
)

object YouTubeProcessingStatus {
  def apply(video: Video): Option[YouTubeProcessingStatus] = {
    val base = YouTubeProcessingStatus(
      video.getId,
      status = "",
      total = 0,
      processed = 0,
      timeLeftMs = 0,
      failure = None
    )

    if (isLiveStream(video)) {
      Some(base.copy(status = "live"))
    } else {
      val summary = video.getStatus

      summary.getUploadStatus match {
        case "uploaded" =>
          Some(parseProcessingStatus(base, video.getProcessingDetails))

        case "failed" =>
          Some(
            base.copy(
              status = "failed",
              failure =
                Option(summary.getFailureReason).map(humanizeFailureReason)
            )
          )

        case "rejected" =>
          Some(
            base.copy(
              status = "failed",
              failure =
                Option(summary.getRejectionReason).map(humanizeRejectionReason)
            )
          )

        case "processed" =>
          None

        case other =>
          Some(base.copy(status = other))
      }
    }
  }

  private def isLiveStream(video: Video): Boolean = {
    Try(video.getSnippet.getLiveBroadcastContent).toOption match {
      case Some("live") => true
      case _            => false
    }
  }

  private def parseProcessingStatus(
      base: YouTubeProcessingStatus,
      details: VideoProcessingDetails
  ): YouTubeProcessingStatus = {
    // progress may be null
    (details.getProcessingStatus, Option(details.getProcessingProgress)) match {
      case ("processing", Some(progress)) =>
        base.copy(
          status = "processing",
          total = progress.getPartsTotal.longValue(),
          processed = progress.getPartsProcessed.longValue(),
          timeLeftMs = progress.getTimeLeftMs.longValue()
        )

      case ("failed", _) =>
        base.copy(
          status = "failed",
          failure = Some(details.getProcessingFailureReason).map(
            humanizeProcessingFailureReason
          )
        )

      case (other, _) =>
        base.copy(status = other)
    }
  }

  private def humanizeFailureReason(reason: String) = reason match {
    case "codec" | "invalidFile" =>
      "The file format is unsupported by YouTube. Perhaps it wasn't a video?"
    case "conversion" =>
      "YouTube was unable to convert the file. Perhaps it wasn't a video?"
    case _ => reason
  }

  private def humanizeRejectionReason(reason: String) = reason match {
    case "claim" | "copyright" =>
      "YouTube has rejected the video for copyright reasons"
    case "duplicate" => "Duplicate of an existing video"
    case "inappropriate" =>
      "YouTube has rejected the video for being inappropriate"
    case "legal"  => "YouTube has rejected the video for legal reasons"
    case "length" => "The video is longer than the YouTube maximum"
    case "termsOfUse" =>
      "YouTube has rejected the video for violating terms of service"
    case "trademark" => "YouTube has rejected the video for trademark violation"
    case reason      => reason
  }

  private def humanizeProcessingFailureReason(reason: String) = reason match {
    case "transcodeFailed" =>
      "YouTube was unable to transcode the file. Perhaps it wasn't a video?"
    case _ => reason
  }
}
