package com.gu.media.upload

import com.gu.media.aws.{
  AwsAccess,
  AwsCredentials,
  MediaConvertAccess,
  StepFunctionsAccess
}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{
  ImageAssetDimensions,
  SelfHostedAsset,
  VideoAsset,
  VideoSource
}
import com.gu.media.upload.model.{
  MediaConvertEvent,
  MediaConvertOutputGroupDetails,
  WaitOnUpload
}
import play.api.libs.json.{Json, Reads}
import software.amazon.awssdk.services.sfn.model.{
  GetExecutionHistoryRequest,
  HistoryEventType,
  SendTaskSuccessRequest,
  SfnException
}

import java.util.Locale
import scala.jdk.CollectionConverters._

class TranscoderComplete
    extends LambdaWithParams[MediaConvertEvent, String]
    with AwsAccess
    with StepFunctionsAccess
    with Logging {

  final override val credentials: AwsCredentials = AwsCredentials.lambda()

  final override def region = AwsAccess.regionFrom(sys.env.get("REGION"))

  final override def readTag(tag: String) =
    sys.env.get(tag.toUpperCase(Locale.ENGLISH))

  override def handle(data: MediaConvertEvent): String = {
    (for {
      executionId <- data.detail.userMetadata
        .get("executionId")
        .toRight("User metadata did not contain executionId")

      lastStateInput <- getLastStateInput[WaitOnUpload](executionId)

      upload = lastStateInput.input

      videoDimensions = getVideoDimensions(data.detail.outputGroupDetails)

      updatedAsset = upload.metadata.asset.map(
        applyDimensionsToAsset(_, videoDimensions)
      )

      output = upload.copy(
        metadata = upload.metadata.copy(asset = updatedAsset),
        progress = upload.progress.copy(retries = 0, fullyTranscoded = true)
      )

      // We extract the taskToken from the input of the current state machine step. It might seem more sensible for this
      // to be passed to this lamba directly via the job user metadata (as with the executionId). But the maximum length
      // of the value of a piece of job metadata is 256 characters and taskTokens are around ~672 characters (real data,
      // I haven't seen documentation on this)

      sendTaskSuccessRequest = SendTaskSuccessRequest
        .builder()
        .taskToken(lastStateInput.taskToken)
        .output(Json.stringify(Json.toJson(output)))
        .build()

      _ <- CatchAWSError(
        stepFunctionsClient.sendTaskSuccess(sendTaskSuccessRequest)
      )
    } yield executionId) match {
      case Left(error) => log.error(error)
      case Right(executionId) =>
        log.info(s"Resumed state machine execution $executionId")
    }

    "Done"
  }

  private def CatchAWSError[T](operation: => T): Either[String, T] = {
    try {
      Right(operation)
    } catch {
      case e: SfnException => Left(e.awsErrorDetails().errorMessage())
    }
  }

  private def getLastStateInput[T](
      executionId: String
  )(implicit fjs: Reads[T]): Either[String, T] = {
    val historyRequest = GetExecutionHistoryRequest
      .builder()
      .executionArn(executionId)
      .reverseOrder(true)
      .build()

    for {
      history <- CatchAWSError(
        stepFunctionsClient.getExecutionHistory(historyRequest)
      )
      events = history.events().asScala
      event <- events
        .find(_.`type` == HistoryEventType.TASK_SCHEDULED)
        .toRight(s"Could not find task scheduled event for $executionId")
    } yield {
      val parameters = event.taskScheduledEventDetails().parameters()
      val payload = Json.parse(parameters) \ "Payload"
      payload.as[T]
    }
  }

  private def getVideoDimensions(
      outputGroups: List[MediaConvertOutputGroupDetails]
  ): Map[String, ImageAssetDimensions] = {
    (for {
      group <- outputGroups
      output <- group.outputDetails
      filename <- output.outputFilePaths.headOption
      videoDetails <- output.videoDetails
      extension <- filename.split('.').lastOption
    } yield {
      extension -> ImageAssetDimensions(
        videoDetails.heightInPx,
        videoDetails.widthInPx
      )
    }).toMap
  }

  private def applyDimensionsToAsset(
      asset: VideoAsset,
      videoDimensions: Map[String, ImageAssetDimensions]
  ): VideoAsset =
    asset match {
      case SelfHostedAsset(sources) =>
        val updatedSources = sources.map { source =>
          val dimensions: Option[ImageAssetDimensions] = source.mimeType match {
            case VideoSource.mimeTypeMp4 =>
              videoDimensions.get("mp4")
            case VideoSource.mimeTypeM3u8 =>
              videoDimensions.get("m3u8")
            case _ => None
          }
          source.copy(
            width = dimensions.map(_.width),
            height = dimensions.map(_.height)
          )
        }
        SelfHostedAsset(updatedSources)
      case _ => asset
    }
}
