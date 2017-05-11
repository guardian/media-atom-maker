package com.gu.media.upload

import com.gu.media.{AddAssetActions, HmacRequestSupport}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with HmacRequestSupport with Logging{
  private val actions = new AddAssetActions(this, this)

  override def handle(upload: Upload): Upload = {
    upload.metadata.youTubeId match {
      case Some(videoId) =>
        actions.addAsset(upload.metadata.pluto.atomId, videoId)

      case None =>
        log.info("Missing YouTube video ID. Cannot add asset")
    }

    log.info(s"Adding asset to ${upload.metadata.pluto.atomId} (not really!!)")

    upload
  }
}
