package com.gu.media

import com.google.api.services.youtube.model.{Channel, Video, VideoCategory}
import com.google.api.services.youtubePartner.model.VideoAdvertisingOption
import com.gu.media.util.ISO8601Duration
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.JavaConverters._
import org.joda.time.DateTime
import com.gu.media.util.JsonDate._

package object youtube {
  case class YouTubeVideoCategory(id: Int, title: String)

  object YouTubeVideoCategory {
    implicit val reads: Reads[YouTubeVideoCategory] = Json.reads[YouTubeVideoCategory]
    implicit val writes: Writes[YouTubeVideoCategory] = Json.writes[YouTubeVideoCategory]

    def build(category: VideoCategory): YouTubeVideoCategory = {
      YouTubeVideoCategory(category.getId.toInt, category.getSnippet.getTitle)
    }
  }
  
  case class YouTubeChannel(id: String, title: String)

  object YouTubeChannel {
    implicit val reads: Reads[YouTubeChannel] = Json.reads[YouTubeChannel]
    implicit val writes: Writes[YouTubeChannel] = Json.writes[YouTubeChannel]

    def build(channel: Channel): YouTubeChannel = {
      YouTubeChannel(
        id = channel.getId,
        title = channel.getSnippet.getTitle
      )
    }
  }

  case class YouTubeVideo (
     id: String,
     title: String,
     duration: Long,
     publishedAt: DateTime,
     privacyStatus: String,
     tags: Seq[String],
     contentBundleTags: Seq[String],
     channel: YouTubeChannel
  )

  object YouTubeVideo {
    implicit val reads: Reads[YouTubeVideo] = Json.reads[YouTubeVideo]
    implicit val writes: Writes[YouTubeVideo] = Json.writes[YouTubeVideo]

    def build(video: Video): YouTubeVideo = {
      val tags: Seq[String] = video.getSnippet.getTags match {
        case null => List()
        case t => t.asScala
      }

      YouTubeVideo(
        id = video.getId,
        title = video.getSnippet.getTitle,
        duration = ISO8601Duration.toSeconds(video.getContentDetails.getDuration),
        publishedAt = new DateTime(video.getSnippet.getPublishedAt.toString),
        privacyStatus = video.getStatus.getPrivacyStatus,
        tags = tags,
        contentBundleTags = tags.filter(t => t.startsWith("gdnpfp")),
        channel = YouTubeChannel(id = video.getSnippet.getChannelId, title = video.getSnippet.getChannelTitle)
      )
    }
  }

  case class YouTubeAdvertising(id: String, adFormats: Seq[String], adBreaks: Seq[String])

  object YouTubeAdvertising {
    implicit val reads: Reads[YouTubeAdvertising] = Json.reads[YouTubeAdvertising]
    implicit val writes: Writes[YouTubeAdvertising] = Json.writes[YouTubeAdvertising]

    def build(videoAdvertisingOption: VideoAdvertisingOption): YouTubeAdvertising = {
      YouTubeAdvertising(
        id = videoAdvertisingOption.getId,
        adFormats = videoAdvertisingOption.getAdFormats.asScala,
        adBreaks = videoAdvertisingOption.getAdBreaks.asScala.map(_.getPosition)
      )
    }
  }

  case class YouTubeVideoCommercialInfo (video: YouTubeVideo, advertising: YouTubeAdvertising)

  object YouTubeVideoCommercialInfo {
    implicit val reads: Reads[YouTubeVideoCommercialInfo] = Json.reads[YouTubeVideoCommercialInfo]
    implicit val writes: Writes[YouTubeVideoCommercialInfo] = Json.writes[YouTubeVideoCommercialInfo]

    def build(video: Video, videoAdvertisingOption: VideoAdvertisingOption): YouTubeVideoCommercialInfo = {
      YouTubeVideoCommercialInfo (
        video = YouTubeVideo.build(video),
        advertising = YouTubeAdvertising.build(videoAdvertisingOption)
      )
    }
  }

  case class YouTubeMetadataUpdate(
    title: Option[String],
    categoryId: Option[String],
    description: Option[String],
    tags: List[String],
    license: Option[String],
    privacyStatus: Option[String]
  ) {
    def withSaneTitle(): YouTubeMetadataUpdate = {
      // Editorial add "- video" for on platform SEO, but it isn't needed on a YouTube video title as its a video platform
      val cleanTitle = this.title.map(_.replaceAll(" (-|–) video( .*)?$", ""))
      this.copy(title = cleanTitle)
    }

    def withContentBundleTags(): YouTubeMetadataUpdate = {
      val contentBundledTags = getContentBundlingTags()
      this.copy(tags = contentBundledTags)
    }

    private def getContentBundlingTags(): List[String] = {
      val contentBundlingMap: Map[String, String] = Map (
        "uk" -> "gdnpfpnewsuk",
        "us" -> "gdnpfpnewsus",
        "au" -> "gdnpfpnewsau",
        "world" -> "gdnpfpnewsworld",
        "politics" -> "gdnpfpnewspolitics",
        "opinion" -> "gdnpfpnewsopinion",
        "football" -> "gdnpfpsportfootball",
        "cricket" -> "gdnpfpsportcricket",
        "rugby union" -> "gdnpfpsportrugbyunion",
        "rugby league" -> "gdnpfpsportrugbyleague",
        "f1" -> "gdnpfpsportf1",
        "tennis" -> "gdnpfpsporttennis",
        "golf" -> "gdnpfpsportgolf",
        "cycling" -> "gdnpfpsportcycling",
        "boxing" -> "gdnpfpsportboxing",
        "racing" -> "gdnpfpsportracing",
        "us sports" -> "gdnpfpsportus",
        "other sport" -> "gdnpfpsportother",
        "other sports" -> "gdnpfpsportother",
        "culture" -> "gdnpfpculture",
        "film" -> "gdnpfpculturefilm",
        "music" -> "gdnpfpculturemusic",
        "lifestyle" -> "gdnpfplifestyle",
        "food" -> "gdnpfplifestylefood",
        "health & fitness" -> "gdnpfplifestylehealthfitness",
        "business" -> "gdnpfpbusiness",
        "money" -> "gdnpfpmoney",
        "fashion" -> "gdnpfpfashion",
        "environment" -> "gdnpfpenvironment",
        "technology" -> "gdnpfptechnology",
        "travel" -> "gdnpfptravel",
        "science" -> "gdnpfpscience",
        "athletics" -> "gdnpfpsportother",
        "basketball" -> "gdnpfpsportus",
        "sport 2.0" -> "gdnpfpsport20"
      )

      this.tags.flatMap { tag =>
        contentBundlingMap.get(tag.toLowerCase()) match {
          case Some(contentBundleTag) => List(tag, contentBundleTag)
          case None => List(tag)
        }
      }
    }
  }

  object YouTubeMetadataUpdate {
    def prettyToString(metadata: YouTubeMetadataUpdate): String = {
      Map(
        "title" -> metadata.title,
        "description" -> metadata.description,
        "tags" -> metadata.tags,
        "categoryId" -> metadata.categoryId,
        "license" -> metadata.license,
        "privacyStatus" -> metadata.privacyStatus.map(_.toString)
      ).collect {
        case (key, Some(value)) =>
          s"\t$key=$value"
      }.mkString("\n")
    }
  }
}
