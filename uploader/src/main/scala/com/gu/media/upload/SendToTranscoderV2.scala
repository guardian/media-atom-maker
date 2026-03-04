package com.gu.media.upload

import software.amazon.awssdk.services.mediaconvert.model.CreateJobRequest
import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
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
    with Logging {
  override def handle(data: WaitOnUpload): Upload = {
    val upload = data.input
    val videoInput = Upload.videoInputUri(upload)
    val maybeSubtitlesInput = Upload.subtitleInputUri(upload)

    val key = TranscoderOutputKey(
      upload.metadata.title,
      upload.metadata.pluto.atomId,
      upload.metadata.version.getOrElse(1L),
      upload.metadata.subtitleVersion.getOrElse(1L),
      None,
      Instant.ofEpochMilli(upload.metadata.startTimestamp)
    ).toString

    val destination = UploadUri(destinationBucket, key).toString

    val jobs = sendToTranscoder(
      videoInput,
      maybeSubtitlesInput,
      data.executionId,
      destination
    )

    val metadata =
      upload.metadata.copy(runtime = SelfHostedUploadMetadata(List(jobs)))

    upload.copy(
      metadata = metadata,
      progress = upload.progress.copy(fullyTranscoded = false)
    )
  }

  private def sendToTranscoder(
      videoInput: UploadUri,
      maybeSubtitlesInput: Option[UploadUri],
      executionId: String,
      destination: String
  ): String = {

    val jobSettings = JobSettingsBuilder.build(
      videoInput.toString,
      maybeSubtitlesInput.map(_.toString),
      destination
    )

    val createJobRequest = CreateJobRequest
      .builder()
      .role(mediaConvertRole)
      .userMetadata(
        Map(
          "stage" -> stage,
          "executionId" -> executionId
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
