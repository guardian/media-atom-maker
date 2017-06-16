package com.gu.media

import java.net.URI

import com.google.api.services.youtube.model.{Channel, VideoCategory}
import com.gu.media.logging.Logging
import com.typesafe.config.Config
import org.cvogt.play.json.Jsonx
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.mutable.ListBuffer

package object youtube {
  class YouTube(override val config: Config) extends Logging with YouTubeAccess with YouTubeVideos

  case class YouTubeVideoCategory(id: Int, title: String)
  case class YouTubeChannel(title: String, logo: URI, id: String)

  object YouTubeVideoCategory {
    implicit val reads: Reads[YouTubeVideoCategory] = Json.reads[YouTubeVideoCategory]
    implicit val writes: Writes[YouTubeVideoCategory] = Json.writes[YouTubeVideoCategory]

    def build(category: VideoCategory): YouTubeVideoCategory = {
      YouTubeVideoCategory(category.getId.toInt, category.getSnippet.getTitle)
    }
  }

  object YouTubeChannel {
    implicit val reads: Reads[YouTubeChannel] = (
      (__ \ "title").read[String] ~
        (__ \ "logo").read[String].map(URI.create) ~
        (__ \ "id").read[String]
      )(YouTubeChannel.apply _)

    implicit val writes: Writes[YouTubeChannel] = (
      (__ \ "title").write[String] ~
        (__ \ "logo").write[String].contramap((_: URI).toString) ~
        (__ \ "id").write[String]
      )(unlift(YouTubeChannel.unapply))

    def build(channel: Channel): YouTubeChannel = {

      YouTubeChannel(
        title = channel.getSnippet().getTitle(),
        logo = URI.create(channel.getSnippet().getThumbnails().getDefault().getUrl()),
        id = channel.getId
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
        "sports" -> "gdnpfpsportother",
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
        "science" -> "gdnpfpscience"
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
