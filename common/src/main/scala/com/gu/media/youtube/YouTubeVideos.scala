package com.gu.media.youtube

import java.io.BufferedInputStream
import java.net.URL

import com.google.api.client.http.InputStreamContent
import com.google.api.services.youtube.model.{Video, VideoProcessingDetails, VideoSnippet}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.logging.Logging
import com.gu.media.util.ISO8601Duration
import cats.syntax.either._

import scala.collection.JavaConverters._

trait YouTubeVideos { this: YouTubeAccess with Logging =>
  def getVideo(youtubeId: String, part: String): Option[Video] = {
    client.videos()
      .list(part)
      .setId(youtubeId)
      .setOnBehalfOfContentOwner(contentOwner)
      .execute()
      .getItems.asScala.toList.headOption
  }

  def getProcessingStatus(videoId: String): Option[YouTubeProcessingStatus] = {
    if(isGuardianVideo(videoId)) {
      val request = client.videos()
        .list("status,processingDetails")
        .setId(videoId)
        .setOnBehalfOfContentOwner(contentOwner)

      request.execute().getItems.asScala.headOption.flatMap(YouTubeProcessingStatus(_))
    } else {
      None
    }
  }

  def getDuration(youtubeId: String): Option[Long] = {
    getVideo(youtubeId, "contentDetails") match {
      case Some(video) => {
        // YouTube API returns duration is in ISO 8601 format
        // https://developers.google.com/youtube/v3/docs/videos#contentDetails.duration
        val iso8601Duration = video.getContentDetails.getDuration
        Some(ISO8601Duration.toSeconds(iso8601Duration))
      }
      case None => None
    }
  }

  def updateMetadata(id: String, metadata: YouTubeMetadataUpdate): Either[String, String] =
    getVideo(id, "snippet, status") match {
      case Some(video) => {

        findMistakesInDev(video) match {
          case None => {

            val oldSnippet = video.getSnippet

            val newSnippet = new VideoSnippet()
            newSnippet.setTags(metadata.tags.asJava)
            newSnippet.setTitle(metadata.title.getOrElse(oldSnippet.getTitle))
            newSnippet.setCategoryId(metadata.categoryId.getOrElse(oldSnippet.getCategoryId))
            newSnippet.setDescription(metadata.description.getOrElse(oldSnippet.getDescription))

            val status = video.getStatus
            metadata.license.foreach(status.setLicense)
            metadata.privacyStatus.map(_.toLowerCase).foreach(status.setPrivacyStatus)

            video.setSnippet(newSnippet)
            video.setStatus(status)

            val prettyMetadata = YouTubeMetadataUpdate.prettyToString(metadata)
            try {
              client.videos()
                .update("snippet, status", video)
                .setOnBehalfOfContentOwner(contentOwner)
                .execute()

              Either.right(prettyMetadata)
            }
            catch {
              case e: Throwable => {
                Either.left(s"unable to update video=$id" + e)
              }
            }
          }
          case Some(error) => Either.left(error)
        }
      }
      case _ => Either.left("could not find video to publish")
    }

  def setStatus(id: String, privacyStatus: PrivacyStatus): Either[String, String] = {
    getVideo(id, "snippet,status") match {
      case Some(video) => {
        findMistakesInDev(video) match {
          case None => {


            video.getStatus.setPrivacyStatus(privacyStatus.name)

            try {
              client.videos()
                .update("snippet, status", video)
                .setOnBehalfOfContentOwner(contentOwner)
                .execute()

              Right(s"marked privacy status as $privacyStatus")
            }
            catch {
              case e: Throwable =>
                Left(s"unable to mark privacy status as $privacyStatus" + e)
            }
          }
          case Some(error) => Left(error)
        }
      }
      case _ => Right(s"no privacy status to update")
    }
  }

  def updateThumbnail(id: String, thumbnailUrl: URL, mimeType: String): Either[String, String] = {
    getVideo(id, "snippet") match {
      case Some(video) => {
        findMistakesInDev(video) match {
          case None => {


            val content = new InputStreamContent(mimeType, new BufferedInputStream(thumbnailUrl.openStream()))
            val set = client.thumbnails().set(id, content).setOnBehalfOfContentOwner(contentOwner)

            // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
            set.getMediaHttpUploader.setDirectUploadEnabled(true)
            set.execute()

            Right("Updated video")
          }
          case Some(error) => Left(error)
        }
      }
      case _ => Left("Could not update thumbnail because could not find video")
    }
  }

  def isGuardianVideo(youtubeId: String): Boolean = {
    getVideo(youtubeId, "snippet") match {
      case Some(video) =>
        val channel = video.getSnippet.getChannelId
        channels.exists(_.id == channel)

      case None =>
        false
    }
  }

  def isManagedVideo(youtubeId: String): Boolean = {
    getVideo(youtubeId, "snippet") match {
      case Some(video) =>
        val channel = video.getSnippet.getChannelId
        allChannels.contains(channel)

      case None =>
        false
    }
  }

  private def findMistakesInDev(video: Video): Option[String] = {
    val videoChannelId = video.getSnippet.getChannelId

    if (disallowedVideos.contains(video.getId)) {
      Some(s"Failed to edit as its in config.youtube.disallowedVideos")
    }

    if (allChannels.nonEmpty && !allChannels.contains(videoChannelId)) {
      Some(s"Failed to edit as its channel ($videoChannelId) isn't in config.youtube.allowedChannels")
    }

    None
  }
}
