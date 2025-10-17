package com.gu.media

import com.google.api.services.youtube.model.{Channel, Video, VideoCategory}
import com.google.api.services.youtubePartner.v1.model.VideoAdvertisingOption
import com.gu.media.model.PrivacyStatus
import com.gu.media.util.ISO8601Duration
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.jdk.CollectionConverters._
import org.joda.time.DateTime
import com.gu.media.util.JsonDate._

package object youtube {

  def contentBundlingMap: Map[String, String] = Map(
    "uk" -> "gdnpfpnewsuk",
    "us" -> "gdnpfpnewsus",
    "au" -> "gdnpfpnewsau",
    "world" -> "gdnpfpnewsworld",
    "politics" -> "gdnpfpnewspolitics",
    "opinion" -> "gdnpfpnewsopinion",
    "football" -> "gdnpfpsportfootball",
    "cricket" -> "gdnpfpsportcricket",
    "rugby-union" -> "gdnpfpsportrugbyunion",
    "rugby-league" -> "gdnpfpsportrugbyleague",
    "f1" -> "gdnpfpsportf1",
    "tennis" -> "gdnpfpsporttennis",
    "golf" -> "gdnpfpsportgolf",
    "cycling" -> "gdnpfpsportcycling",
    "boxing" -> "gdnpfpsportboxing",
    "racing" -> "gdnpfpsportracing",
    "us-sport" -> "gdnpfpsportus",
    "sport" -> "gdnpfpsportother",
    "culture" -> "gdnpfpculture",
    "film" -> "gdnpfpculturefilm",
    "music" -> "gdnpfpculturemusic",
    "lifestyle" -> "gdnpfplifestyle",
    "food" -> "gdnpfplifestylefood",
    "health-and-wellbeing" -> "gdnpfplifestylehealthfitness",
    "business" -> "gdnpfpbusiness",
    "money" -> "gdnpfpmoney",
    "fashion" -> "gdnpfpfashion",
    "environment" -> "gdnpfpenvironment",
    "technology" -> "gdnpfptechnology",
    "travel" -> "gdnpfptravel",
    "science" -> "gdnpfpscience",
    "athletics" -> "gdnpfpsportother",
    "basketball" -> "gdnpfpsportus",
    "sport-2-0" -> "gdnpfpsport20"
  )

  case class YouTubeVideoCategory(id: Int, title: String)

  object YouTubeVideoCategory {
    implicit val reads: Reads[YouTubeVideoCategory] =
      Json.reads[YouTubeVideoCategory]
    implicit val writes: Writes[YouTubeVideoCategory] =
      Json.writes[YouTubeVideoCategory]

    def build(category: VideoCategory): YouTubeVideoCategory = {
      YouTubeVideoCategory(category.getId.toInt, category.getSnippet.getTitle)
    }
  }

  case class YouTubeChannelWithData(
      id: String,
      title: String,
      privacyStates: Set[PrivacyStatus] = PrivacyStatus.all,
      isCommercial: Boolean
  )

  object YouTubeChannelWithData {
    implicit val reads: Reads[YouTubeChannelWithData] =
      Json.reads[YouTubeChannelWithData]
    implicit val writes: Writes[YouTubeChannelWithData] =
      Json.writes[YouTubeChannelWithData]

    private def getPrivacyStates(
        id: String,
        hasMakePublicPermission: Boolean,
        youtubeAccess: YouTubeAccess
    ): Set[PrivacyStatus] = {
      if (!youtubeAccess.channelsRequiringPermission.contains(id)) {
        PrivacyStatus.all
      } else {
        if (hasMakePublicPermission) PrivacyStatus.all
        else Set(PrivacyStatus.Unlisted, PrivacyStatus.Private)
      }
    }

    def build(
        youtubeAccess: YouTubeAccess,
        id: String,
        title: String,
        hasMakePublicPermission: Boolean
    ): YouTubeChannelWithData = {
      YouTubeChannelWithData(
        id = id,
        title = title,
        privacyStates =
          getPrivacyStates(id, hasMakePublicPermission, youtubeAccess),
        isCommercial = youtubeAccess.commercialChannels.contains(id)
      )
    }
  }

  case class YouTubeChannel(
      id: String,
      title: String
  )

  object YouTubeChannel {
    implicit val reads: Reads[YouTubeChannel] = Json.reads[YouTubeChannel]
    implicit val writes: Writes[YouTubeChannel] = Json.writes[YouTubeChannel]

    def build(
        youtubeAccess: YouTubeAccess,
        channel: Channel
    ): YouTubeChannel = {
      val id = channel.getId
      val title = channel.getSnippet.getTitle

      build(youtubeAccess, id, title)
    }

    def build(
        youtubeAccess: YouTubeAccess,
        id: String,
        title: String
    ): YouTubeChannel = {
      YouTubeChannel(
        id = id,
        title = title
      )
    }
  }

  case class YouTubeVideoDetail(
      id: String,
      title: String,
      duration: Long,
      publishedAt: DateTime,
      privacyStatus: String,
      tags: Seq[String],
      contentBundleTags: Seq[String],
      channel: YouTubeChannel
  )

  object YouTubeVideoDetail {
    implicit val reads: Reads[YouTubeVideoDetail] =
      Json.reads[YouTubeVideoDetail]
    implicit val writes: Writes[YouTubeVideoDetail] =
      Json.writes[YouTubeVideoDetail]

    def build(video: Video, channel: YouTubeChannel): YouTubeVideoDetail = {
      val tags: Seq[String] =
        Option(video.getSnippet.getTags).toSeq.flatMap(_.asScala)

      YouTubeVideoDetail(
        id = video.getId,
        title = video.getSnippet.getTitle,
        duration =
          ISO8601Duration.toSeconds(video.getContentDetails.getDuration),
        publishedAt = new DateTime(video.getSnippet.getPublishedAt.toString),
        privacyStatus = video.getStatus.getPrivacyStatus,
        tags = tags,
        contentBundleTags = tags.filter(t => t.startsWith("gdnpfp")),
        channel = channel
      )
    }
  }

  case class YouTubeAdvertising(
      id: String,
      adFormats: Seq[String],
      adBreaks: Seq[String]
  )

  object YouTubeAdvertising {
    implicit val reads: Reads[YouTubeAdvertising] =
      Json.reads[YouTubeAdvertising]
    implicit val writes: Writes[YouTubeAdvertising] =
      Json.writes[YouTubeAdvertising]

    def build(
        videoAdvertisingOption: VideoAdvertisingOption
    ): YouTubeAdvertising = {
      YouTubeAdvertising(
        id = videoAdvertisingOption.getId,
        adFormats = videoAdvertisingOption.getAdFormats.asScala.toSeq,
        adBreaks =
          videoAdvertisingOption.getAdBreaks.asScala.toSeq.map(_.getPosition)
      )
    }
  }

  case class YouTubeVideoCommercialInfo(
      video: YouTubeVideoDetail,
      advertising: YouTubeAdvertising
  )

  object YouTubeVideoCommercialInfo {
    implicit val reads: Reads[YouTubeVideoCommercialInfo] =
      Json.reads[YouTubeVideoCommercialInfo]
    implicit val writes: Writes[YouTubeVideoCommercialInfo] =
      Json.writes[YouTubeVideoCommercialInfo]

    def build(
        video: Video,
        videoAdvertisingOption: VideoAdvertisingOption,
        channel: YouTubeChannel
    ): YouTubeVideoCommercialInfo = {
      YouTubeVideoCommercialInfo(
        video = YouTubeVideoDetail.build(video, channel),
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
      val cleanTitle = this.title.map(
        _.replaceAll(
          "\u00a0",
          " "
        ) // replace &nbsp; with actual whitespace because ¯\_(ツ)_/¯
          .replaceAll(" (-|–) video( .*)?$", "")
      )
      this.copy(title = cleanTitle)
    }

    def withContentBundleTags(): YouTubeMetadataUpdate = {
      val contentBundledTags = getContentBundlingTags()
      this.copy(tags = contentBundledTags)
    }

    def withoutContentBundleTags(): YouTubeMetadataUpdate = {
      val noContentBundleTags = tags.filterNot(_.startsWith("gdnpfp"))
      this.copy(tags = noContentBundleTags)
    }

    private def getContentBundlingTags(): List[String] = {

      this.tags.flatMap { tag =>
        contentBundlingMap.get(tag.toLowerCase()) match {
          case Some(contentBundleTag) => List(tag, contentBundleTag)
          case None                   => List(tag)
        }
      }
    }
  }

  object YouTubeMetadataUpdate {
    def prettyToString(metadata: YouTubeMetadataUpdate): String = {
      Map(
        "title" -> metadata.title,
        "description" -> metadata.description,
        "tags" -> Some(metadata.tags),
        "categoryId" -> metadata.categoryId,
        "license" -> metadata.license,
        "privacyStatus" -> metadata.privacyStatus.map(_.toString)
      ).collect { case (key, Some(value)) =>
        s"\t$key=$value"
      }.mkString("\n")
    }
  }
}
