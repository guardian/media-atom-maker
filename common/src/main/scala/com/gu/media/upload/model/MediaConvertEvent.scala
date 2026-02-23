package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format

case class MediaConvertEvent(
  version: String,
  id: String,
  `detail-type`: String,
  source: String,
  account: String,
  time: String,
  region: String,
  resources: List[String],
  detail: MediaConvertEventDetail
)

object MediaConvertEvent {
  implicit val format: Format[MediaConvertEvent] = Jsonx.formatCaseClass[MediaConvertEvent]
}

case class MediaConvertWarning(code: Int, count: Int)

object MediaConvertWarning {
  implicit val format: Format[MediaConvertWarning] = Jsonx.formatCaseClass[MediaConvertWarning]
}

case class MediaConvertStartEnd(start: Int, end: Int)

object MediaConvertStartEnd {
  implicit val format: Format[MediaConvertStartEnd] = Jsonx.formatCaseClass[MediaConvertStartEnd]
}

case class MediaConvertEventDetail(
  timestamp: Long,
  accountId: String,
  queue: String,
  jobId: String,
  status: String,
  userMetadata: Map[String, String],
  warnings: List[MediaConvertWarning],
  outputGroupDetails: List[MediaConvertOutputGroupDetails],
  paddingInserted: Int,
  blackVideoDetected: Int,
  blackSegments: Option[List[MediaConvertStartEnd]],
  blackVideoSegments: Option[List[MediaConvertStartEnd]]
)

object MediaConvertEventDetail {
  implicit val format: Format[MediaConvertEventDetail] = Jsonx.formatCaseClass[MediaConvertEventDetail]
}

case class MediaConvertOutputGroupDetails(
  outputDetails: List[MediaConvertOutputDetails],
  playlistFilePaths: Option[List[String]],
  `type`: String
)

object MediaConvertOutputGroupDetails {
  implicit val format: Format[MediaConvertOutputGroupDetails] = Jsonx.formatCaseClass[MediaConvertOutputGroupDetails]
}

case class MediaConvertVideoDetails(
  widthInPx: Int,
  heightInPx: Int,
  qvbrAvgQuality: Option[Float],
  qvbrMinQuality: Option[Float],
  qvbrMaxQuality: Option[Float],
  qvbrMinQualityLocation: Option[Float],
  qvbrMaxQualityLocation: Option[Float],
  averageBitrate: Option[Float]
)

object MediaConvertVideoDetails {
  implicit val format: Format[MediaConvertVideoDetails] = Jsonx.formatCaseClass[MediaConvertVideoDetails]
}

case class MediaConvertOutputDetails(
  outputFilePaths: List[String],
  durationInMs: Long,
  videoDetails: Option[MediaConvertVideoDetails]
)

object MediaConvertOutputDetails {
  implicit val format: Format[MediaConvertOutputDetails] = Jsonx.formatCaseClass[MediaConvertOutputDetails]
}
