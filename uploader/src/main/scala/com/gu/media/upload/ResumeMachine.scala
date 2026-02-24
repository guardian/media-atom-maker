package com.gu.media.upload

import com.gu.media.aws.{MediaConvertAccess, StepFunctionsAccess}
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

import scala.jdk.CollectionConverters._

class ResumeMachine
    extends LambdaWithParams[MediaConvertEvent, String]
    with MediaConvertAccess
    with StepFunctionsAccess
    with Logging {
  override def handle(data: MediaConvertEvent): String = {
    (for {
      executionId <- data.detail.userMetadata
        .get("executionId")
        .toRight("User metadata did not contain executionId")

      lastSateInput <- getLastStateInput[WaitOnUpload](executionId)

      upload = lastSateInput.input

      videoDimensions = getVideoDimensions(data.detail.outputGroupDetails)

      updatedAsset = upload.metadata.asset.map(
        applyDimensionsToAsset(_, videoDimensions)
      )

      output = upload.copy(
        metadata = upload.metadata.copy(asset = updatedAsset),
        progress = upload.progress.copy(retries = 0, fullyTranscoded = true)
      )

      sendTaskSuccessRequest = SendTaskSuccessRequest
        .builder()
        .taskToken(lastSateInput.taskToken)
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
