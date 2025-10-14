package com.gu.media.logging

import enumeratum.EnumEntry.Hyphencase
import enumeratum.{Enum, EnumEntry}
import net.logstash.logback.marker.{LogstashMarker, Markers}

import scala.jdk.CollectionConverters._

sealed trait YoutubeApiType extends EnumEntry with Hyphencase

object YoutubeApiType extends Enum[YoutubeApiType] {
  val values = findValues

  case object DataApi extends YoutubeApiType
  case object PartnerApi extends YoutubeApiType
  case object UploadApi extends YoutubeApiType
}

sealed trait YoutubeRequestType extends EnumEntry with Hyphencase

object YoutubeRequestType extends Enum[YoutubeRequestType] {
  val values = findValues

  case object ListChannels extends YoutubeRequestType
  case object ListCategories extends YoutubeRequestType
  case object GetVideo extends YoutubeRequestType
  case object GetProcessingStatus extends YoutubeRequestType

  case object UpdateVideoMetadata extends YoutubeRequestType
  case object UpdateVideoPrivacyStatus extends YoutubeRequestType
  case object UpdateVideoThumbnail extends YoutubeRequestType

  case object CreateAsset extends YoutubeRequestType
  case object SetOwnership extends YoutubeRequestType
  case object CreateVideoClaim extends YoutubeRequestType
  case object UpdateVideoClaim extends YoutubeRequestType
  case object GetVideoClaim extends YoutubeRequestType
  case object GetVideoAdvertisingOptions extends YoutubeRequestType
  case object UpdateVideoAdvertisingOptions extends YoutubeRequestType

  case object DeleteVideo extends YoutubeRequestType

  case object StartVideoUpload extends YoutubeRequestType
  case object UploadVideoChunk extends YoutubeRequestType
}

object YoutubeRequestLogger extends Logging {
  def logRequest(apiType: YoutubeApiType, requestType: YoutubeRequestType) = {
    val markers: LogstashMarker = Markers.appendEntries(
      Map(
        "youtubeApiType" -> apiType.entryName,
        "youtubeApiRequestType" -> requestType.entryName
      ).asJava
    )

    log.info(markers, "Calling Youtube API")
  }
}
