package com.gu.media.upload

import com.gu.media.aws.DynamoAccess
import com.gu.media.{AddAssetActions, HmacRequestSupport}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with DynamoAccess with HmacRequestSupport with Logging {
  private val actions = new AddAssetActions(this, this)
  private val table = new UploadsDataStore(this)

  override def handle(upload: Upload): Upload = {
    upload.metadata.youTubeId match {
      case Some(videoId) =>
        actions.addAsset(upload.metadata.pluto.atomId, videoId)

      case None =>
        throw new IllegalStateException("Missing YouTube video ID. Cannot add asset")
    }

    table.delete(upload.id)
    upload
  }
}
