package com.gu.media.upload

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.media.HmacRequestSupport
import com.gu.media.aws.DynamoAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload
import com.gu.media.util.MediaAtomHelpers._
import com.gu.media.util.YouTubeAsset

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with DynamoAccess with HmacRequestSupport with Logging {
  private val store = new PreviewDynamoDataStore(this.dynamoDB, this.dynamoTableName)

  override def handle(upload: Upload): Upload = {
    // TODO MRB: add self hosted asset
    val atomId = upload.metadata.pluto.atomId

    upload.metadata.youTubeId match {
      case Some(videoId) =>
        store.getAtom(atomId) match {
          case Right(before) =>
            store.updateAtom(
              updateAtom(before) { mediaAtom =>
                addAsset(mediaAtom, YouTubeAsset(videoId))
              }
            )

          case Left(err) =>
            throw new IllegalStateException(s"${err.getMessage}. Cannot add asset", err)
        }

      case None =>
        throw new IllegalStateException("Missing YouTube video ID. Cannot add asset")
    }

    upload
  }
}
