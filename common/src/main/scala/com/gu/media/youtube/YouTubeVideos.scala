package com.gu.media.youtube

import com.google.api.client.googleapis.json.{
  GoogleJsonError,
  GoogleJsonResponseException
}
import com.google.api.client.http.InputStreamContent
import com.google.api.services.youtube.model.{Video, VideoSnippet}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.logging.Logging
import com.gu.media.model.VideoUpdateError
import com.gu.media.util.ISO8601Duration
import com.gu.media.logging.{
  YoutubeApiType,
  YoutubeRequestLogger,
  YoutubeRequestType
}

import scala.jdk.CollectionConverters._

trait YouTubeVideos { this: YouTubeAccess with Logging =>
  def getVideo(youtubeId: String, part: List[String]): Option[Video] = {
    val request = client
      .videos()
      .list(part.asJava)
      .setId(List(youtubeId).asJava)
      .setOnBehalfOfContentOwner(contentOwner)

    YoutubeRequestLogger.logRequest(
      YoutubeApiType.DataApi,
      YoutubeRequestType.GetVideo
    )
    request.execute().getItems.asScala.toList.headOption
  }

  def getProcessingStatus(videoId: String): Option[YouTubeProcessingStatus] = {
    if (isGuardianVideo(videoId)) {
      val request = client
        .videos()
        .list(List("snippet", "status", "processingDetails").asJava)
        .setId(List(videoId).asJava)
        .setOnBehalfOfContentOwner(contentOwner)

      YoutubeRequestLogger.logRequest(
        YoutubeApiType.DataApi,
        YoutubeRequestType.GetProcessingStatus
      )
      request
        .execute()
        .getItems
        .asScala
        .headOption
        .flatMap(YouTubeProcessingStatus(_))
    } else {
      None
    }
  }

  def getDuration(youtubeId: String): Option[Long] = {
    getVideo(youtubeId, List("contentDetails")) match {
      case Some(video) => {
        // YouTube API returns duration is in ISO 8601 format
        // https://developers.google.com/youtube/v3/docs/videos#contentDetails.duration
        val iso8601Duration = video.getContentDetails.getDuration

        log.info(
          s"YouTube reported a duration of $iso8601Duration for asset: $youtubeId"
        )

        Some(ISO8601Duration.toSeconds(iso8601Duration))
      }
      case None => {
        log.info(s"Failed to get duration for YouTube asset: $youtubeId")
        None
      }
    }
  }

  def updateMetadata(
      id: String,
      metadata: YouTubeMetadataUpdate
  ): Either[VideoUpdateError, String] =
    getVideo(id, List("snippet", "status")) match {
      case Some(video) => {

        findMistakesInDev(video) match {
          case None => {

            val oldSnippet = video.getSnippet

            val newSnippet = new VideoSnippet()
            newSnippet.setTags(metadata.tags.asJava)
            newSnippet.setTitle(metadata.title.getOrElse(oldSnippet.getTitle))
            newSnippet.setCategoryId(
              metadata.categoryId.getOrElse(oldSnippet.getCategoryId)
            )
            newSnippet.setDescription(
              metadata.description.getOrElse(oldSnippet.getDescription)
            )

            val status = video.getStatus
            metadata.license.foreach(status.setLicense)
            metadata.privacyStatus
              .map(_.toLowerCase)
              .foreach(status.setPrivacyStatus)

            video.setSnippet(newSnippet)
            video.setStatus(status)

            val prettyMetadata = YouTubeMetadataUpdate.prettyToString(metadata)
            try {
              val request = client
                .videos()
                .update(List("snippet", "status").asJava, video)
                .setOnBehalfOfContentOwner(contentOwner)

              YoutubeRequestLogger.logRequest(
                YoutubeApiType.DataApi,
                YoutubeRequestType.UpdateVideoMetadata
              )
              request.execute()

              Right(prettyMetadata)
            } catch {
              case e: GoogleJsonResponseException => {
                val error: GoogleJsonError = e.getDetails
                val message = error
                Left(
                  VideoUpdateError(
                    error.toString + "\n metadata: " + prettyMetadata,
                    Some(error.getMessage)
                  )
                )
              }
            }
          }
          case Some(error) => Left(VideoUpdateError(error))
        }
      }
      case _ => Left(VideoUpdateError("could not find video to publish"))
    }

  def setStatus(
      id: String,
      privacyStatus: PrivacyStatus
  ): Either[VideoUpdateError, String] = {
    getVideo(id, List("snippet", "status")) match {
      case Some(video) => {
        findMistakesInDev(video) match {
          case None => {

            video.getStatus.setPrivacyStatus(privacyStatus.name)

            try {
              val request = client
                .videos()
                .update(List("snippet", "status").asJava, video)
                .setOnBehalfOfContentOwner(contentOwner)

              YoutubeRequestLogger.logRequest(
                YoutubeApiType.DataApi,
                YoutubeRequestType.UpdateVideoPrivacyStatus
              )
              request.execute()

              Right(s"marked privacy status as ${privacyStatus.name}")
            } catch {
              case e: GoogleJsonResponseException =>
                val error: GoogleJsonError = e.getDetails
                Left(VideoUpdateError(error.getMessage, Some(error.toString)))
            }
          }
          case Some(error) => Left(VideoUpdateError(error))
        }
      }
      case _ => Right(s"no privacy status to update")
    }
  }

  def updateThumbnail(
      id: String,
      thumbnail: InputStreamContent
  ): Either[VideoUpdateError, String] = {
    getVideo(id, List("snippet")) match {
      case Some(video) => {
        findMistakesInDev(video) match {
          case None => {
            val set = client
              .thumbnails()
              .set(id, thumbnail)
              .setOnBehalfOfContentOwner(contentOwner)

            // If we want some way of monitoring and resuming thumbnail uploads then we can change this to be `false`
            set.getMediaHttpUploader.setDirectUploadEnabled(true)

            YoutubeRequestLogger.logRequest(
              YoutubeApiType.DataApi,
              YoutubeRequestType.UpdateVideoThumbnail
            )
            set.execute()

            Right("Updated video")
          }
          case Some(error) =>
            Left(
              VideoUpdateError("Could not update video thumbnail", Some(error))
            )
        }
      }
      case _ =>
        Left(
          VideoUpdateError(
            "Could not update thumbnail because could not find video"
          )
        )
    }
  }

  def isGuardianVideo(youtubeId: String): Boolean = {
    getVideo(youtubeId, List("snippet")) match {
      case Some(video) =>
        val channel = video.getSnippet.getChannelId
        channels.exists(_.id == channel)

      case None =>
        false
    }
  }

  def isManagedVideo(youtubeId: String): Boolean = {
    getVideo(youtubeId, List("snippet")) match {
      case Some(video) =>
        val channel = video.getSnippet.getChannelId
        allChannels.contains(channel)

      case None =>
        false
    }
  }

  private def findMistakesInDev(video: Video): Option[String] = {
    val videoChannelId = video.getSnippet.getChannelId

    if (disallowedVideos.contains(video.getId))
      Some(s"Failed to edit as its in config.youtube.disallowedVideos")

    if (allChannels.nonEmpty && !allChannels.contains(videoChannelId))
      Some(
        s"Failed to edit as its channel ($videoChannelId) isn't in config.youtube.allowedChannels"
      )

    None
  }
}
