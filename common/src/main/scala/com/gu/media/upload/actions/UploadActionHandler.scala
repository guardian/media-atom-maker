package com.gu.media.upload.actions

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.elastictranscoder.model.{CreateJobOutput, CreateJobRequest, JobInput}
import com.gu.media.PlutoDataStore
import com.gu.media.logging.Logging
import com.gu.media.ses.Mailer
import com.gu.media.upload._
import com.gu.media.upload.model.{PlutoSyncMetadata, Upload}
import com.gu.media.youtube.YouTubeUploader

abstract class UploadActionHandler(store: UploadsDataStore, plutoStore: PlutoDataStore, uploaderAccess: UploaderAccess,
                                   youTube: YouTubeUploader, mailer: Mailer)
  extends Logging {

  private val s3 = new S3UploadActions(uploaderAccess.s3Client)
  private val transcoderClient = uploaderAccess.transcoderClient
  transcoderClient.setRegion(Region.getRegion(Regions.EU_WEST_1))
  private val TRANSCODER_PRESET_ID = "1351620000001-000001" //System preset: Generic 1080p

  // Returns the new version number
  def addAsset(atomId: String, videoId: String): Long

  def handle(action: UploadAction): Unit = action match {
    case _ if action.upload.metadata.useStepFunctions =>
      log.info("Bypassing kinesis powered lambdas")

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
      sendToPluto(upload)

    case UploadPartsToSelfHost(upload, key, pipelineId) =>
      transcodeToS3(key, pipelineId)

    case DeleteParts(upload) =>
      s3.deleteParts(upload)
      store.delete(upload.id)

  }

  private def sendToPluto(upload: Upload): Unit = {
    val plutoData: PlutoSyncMetadata = upload.metadata.pluto

    if(!plutoData.enabled) {
      log.info(s"Not syncing to Pluto upload=${upload.id} atom=${plutoData.atomId}")
    } else {
      plutoData.projectId match {
        case Some(project) =>
          uploaderAccess.sendOnKinesis(uploaderAccess.uploadsStreamName, plutoData.s3Key, plutoData)

        case None =>
          val metadata = upload.metadata
          log.info(s"Sending missing Pluto ID email user=${metadata.user} atom=${plutoData.atomId}")
          mailer.sendPlutoIdMissingEmail(metadata.title, metadata.user, uploaderAccess.fromEmailAddress,
            uploaderAccess.replyToAddresses)
          plutoStore.put(plutoData)
      }
    }
  }

  private def transcodeToS3(fileName: String, pipelineId: String) {

    val jobInput = new JobInput().withKey(fileName)

    val mp4FileName = fileName + ".mp4"

    val jobOutput = new CreateJobOutput()
      .withKey(mp4FileName)
      .withPresetId(TRANSCODER_PRESET_ID)

    val createJobRequest = new CreateJobRequest()
      .withPipelineId(pipelineId)
      .withInput(jobInput)
      .withOutput(jobOutput)

    transcoderClient.createJob(createJobRequest).getJob
    log.info(s"Sent $fileName to transcoder output will be $mp4FileName")
  }
}
