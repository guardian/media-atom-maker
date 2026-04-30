package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.CreateJobRequest
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
<<<<<<< HEAD
import com.gu.media.telemetry.Telemetry
=======
import com.gu.media.upload.FfMpeg.checkAudioExists
>>>>>>> main
import com.gu.media.upload.mediaconvert.JobSettingsBuilder
import com.gu.media.upload.model.{
  SelfHostedUploadMetadata,
  Upload,
  WaitOnUpload
}

import java.time.Instant
import scala.jdk.CollectionConverters._

class SendToTranscoderV2
    extends LambdaWithParams[WaitOnUpload, Upload]
    with LambdaBase
    with MediaConvertAccess
    with S3Helpers
    with Logging {
  override def handle(data: WaitOnUpload, telemetry: Telemetry): Upload = {
    val tags = telemetry.createTags(data.input)
    telemetry.sendTelemetryEvent("LAMBDA_START_SendToTranscoderV2", tags)
    val upload = data.input
    val videoInput = Upload.videoInputUri(upload)
    val maybeSubtitlesInput = Upload.subtitleInputUri(upload)

    // Presign a GET URL so ffmpeg can read the video directly over HTTPS
    val presignedUrl = generatePresignedDownloadUrl(
      bucket = videoInput.bucket,
      key = videoInput.key
    )

    // validate audio boolean using:
    val hasAudio = checkAudioExists(presignedUrl)

    val key = TranscoderOutputKey(
      upload.metadata.title,
      upload.metadata.pluto.atomId,
      upload.metadata.version.getOrElse(1L),
      upload.metadata.subtitleVersion.getOrElse(0L),
      None,
      Instant.ofEpochMilli(upload.metadata.startTimestamp)
    ).toString

    val destination = UploadUri(destinationBucket, key).toString

    val jobs = sendToTranscoder(
      videoInput,
      maybeSubtitlesInput,
      data.executionId,
      destination,
      hasAudio
    )

    val metadata = upload.metadata.copy(
      runtime = SelfHostedUploadMetadata(
        jobs = Some(List(jobs))
      )
    )
    upload.metadata.copy(runtime = SelfHostedUploadMetadata(Some(List(jobs))))
    telemetry.sendTelemetryEvent("LAMBDA_END_SendToTranscoderV2", tags)
    upload.copy(
      metadata = metadata,
      progress = upload.progress.copy(fullyTranscoded = false)
    )
  }

  private def sendToTranscoder(
      videoInput: UploadUri,
      maybeSubtitlesInput: Option[UploadUri],
      executionId: String,
      destination: String,
      hasAudio: Boolean
  ): String = {

    val jobSettings = JobSettingsBuilder.build(
      videoInput.toString,
      maybeSubtitlesInput.map(_.toString),
      destination,
      hasAudio
    )

    val createJobRequest = CreateJobRequest
      .builder()
      .role(mediaConvertRole)
      .userMetadata(
        Map(
          "stage" -> stage,
          "executionId" -> executionId,
          "hasAudio" -> hasAudio.toString
        ).asJava
      )
      .settings(jobSettings)
      .build()

    log.info("Creating job: " + createJobRequest.toString)

    val job = mediaConvertClient.createJob(createJobRequest).job()
    val id = job.id()

    log.info(s"Sent $videoInput to mediaconvert (id: $id)")

    id
  }
}
