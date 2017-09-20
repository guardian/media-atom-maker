package com.gu.media.youtube

import java.io.BufferedInputStream
import java.net.URL

import com.google.api.client.http.InputStreamContent
import com.google.api.services.youtube.model.{Video, VideoProcessingDetails, VideoSnippet}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.logging.Logging
import com.gu.media.util.ISO8601Duration

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

      request.execute().getItems.asScala.headOption.map(YouTubeProcessingStatus(_))
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

  def updateMetadata(id: String, metadata: YouTubeMetadataUpdate): Option[Video] =
    getVideo(id, "snippet, status") match {
      case Some(video) =>
        protectAgainstMistakesInDev(video)

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

        log.info(s"Updating YouTube metadata for $id:\n${YouTubeMetadataUpdate.prettyToString(metadata)}")

        Some(client.videos()
          .update("snippet, status", video)
          .setOnBehalfOfContentOwner(contentOwner)
          .execute())
      case _ => None
    }

  def setStatus(id: String, privacyStatus: PrivacyStatus): Unit = {
    getVideo(id, "snippet,status") match {
      case Some(video) => {
        protectAgainstMistakesInDev(video)

        video.getStatus.setPrivacyStatus(privacyStatus.name)

        try {
          Some(client.videos()
            .update("snippet, status", video)
            .setOnBehalfOfContentOwner(contentOwner)
            .execute())

          log.info(s"marked asset=$id as $privacyStatus")
        }
        catch {
          case e: Throwable =>
            log.warn(s"unable to mark asset=$id as $privacyStatus", e)
        }
      }
      case _ =>
    }
  }

  def updateThumbnail(id: String, thumbnailUrl: URL, mimeType: String): Option[Video] = {
    getVideo(id, "snippet") match {
      case Some(video) => {
        protectAgainstMistakesInDev(video)

        val content = new InputStreamContent(mimeType, new BufferedInputStream(thumbnailUrl.openStream()))
        val set = client.thumbnails().set(id, content).setOnBehalfOfContentOwner(contentOwner)

        // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
        set.getMediaHttpUploader.setDirectUploadEnabled(true)
        set.execute()

        Some(video)
      }
      case _ => None
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

  private def protectAgainstMistakesInDev(video: Video) = {
    val videoChannelId = video.getSnippet.getChannelId

    if (disallowedVideos.contains(video.getId)) {
      val msg = s"Failed to edit video ${video.getId} as its in config.youtube.disallowedVideos"
      log.info(msg)
      throw new Exception(msg)
    }

    if (allChannels.nonEmpty && !allChannels.contains(videoChannelId)) {
      val msg = s"Failed to edit video ${video.getId} as its channel ($videoChannelId) isn't in config.youtube.allowedChannels"
      log.info(msg)
      throw new Exception(msg)
    }
  }
}
