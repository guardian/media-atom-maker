package com.gu.media.upload.actions

import com.gu.media.PlutoDataStore
import com.gu.media.aws.AwsAccess.UploaderAccess
import com.gu.media.logging.Logging
import com.gu.media.upload._
import com.gu.media.youtube.YouTubeUploader

abstract class UploadActionHandler(store: UploadsDataStore, plutoStore: PlutoDataStore, uploaderAccess: UploaderAccess,
                                   youTube: YouTubeUploader)
  extends Logging {

  private val s3 = new S3UploadActions(uploaderAccess.s3Client)
  private val pluto = new PlutoUploadActions(uploaderAccess)
  private val transcoder = new TranscodeUploadActions(uploaderAccess.transcoderClient)

  // Returns the new version number
  def addAsset(atomId: String, videoId: String): Long

  def handle(action: UploadAction): Unit = action match {
    case UploadPartToYouTube(upload, part, uploadUri) =>
      val updated = youTube.uploadPart(upload, part, uploadUri)

      store.report(updated.metadata.youTubeId match {
        case Some(videoId) =>
          val version = addAsset(upload.metadata.pluto.atomId, videoId)
          val plutoData = upload.metadata.pluto.copy(assetVersion = version)

          updated.copy(metadata = upload.metadata.copy(pluto = plutoData))

        case _ =>
          updated
      })

    case CopyParts(upload, destination) =>
      s3.createCompleteObject(upload, destination)

      if(upload.metadata.pluto.enabled) {
        pluto.sendToPluto(upload.metadata)
      } else {
        log.info(s"Not syncing to Pluto upload=${upload.id} atom=${upload.metadata.pluto.atomId}")
      }

    case UploadPartsToSelfHost(upload, key, pipelineId) =>
      transcoder.transcodeToS3(key, pipelineId)

    case DeleteParts(upload) =>
      s3.deleteParts(upload)
      store.delete(upload.id)

  }
}
