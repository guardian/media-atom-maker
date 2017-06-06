package com.gu.media.upload

import com.gu.media.aws.DynamoAccess
import com.gu.media.{AddAssetActions, HmacRequestSupport}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.media.youtube.{YouTubeAccess, YouTubeVideos}

class AddYouTubeAsset extends LambdaWithParams[Upload, Upload]
  with DynamoAccess
  with YouTubeAccess
  with YouTubeVideos
  with HmacRequestSupport
  with Logging {

  private val actions = new AddAssetActions(this, this)
  private val table = new UploadsDataStore(this)

  override def handle(upload: Upload): Upload = {
    upload.metadata.youTubeId match {
      case Some(videoId) =>
        addYouTubeAsset(upload.metadata.pluto.atomId, videoId)

      case None =>
        throw new IllegalStateException("Missing YouTube video ID. Cannot add asset")
    }

    table.delete(upload.id)
    upload.copy(progress = upload.progress.copy(assetAdded = true))
  }

  private def addYouTubeAsset(atomId: String, youTubeId: String): Unit = {
    getVideo(youTubeId, "status") match {
      case Some(video) =>
        Option(video.getStatus.getRejectionReason) match {
          case Some(err) =>
            throw new IllegalStateException(s"YouTube rejected: $err")

          case None =>
            actions.addAsset(atomId, youTubeId)
        }

      case None =>
        throw new IllegalStateException(s"YouTube video $youTubeId does not exist. Cannot add asset")
    }
  }
}
