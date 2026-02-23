package com.gu.media.upload

import com.gu.media.aws.MediaConvertAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.{MediaConvertEvent, WaitOnUpload}
import play.api.libs.json.Json
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.{
  GetExecutionHistoryRequest,
  HistoryEventType,
  SendTaskSuccessRequest
}

import scala.jdk.CollectionConverters._

class ResumeMachine
    extends LambdaWithParams[MediaConvertEvent, String]
    with MediaConvertAccess
    with Logging {
  override def handle(data: MediaConvertEvent): String = {
    val stepFunctionsClient = SfnClient
      .builder()
      .credentialsProvider(credentials.instance.awsV2Creds)
      .region(awsV2Region)
      .build()

    val executionId = data.detail.userMetadata.get("executionId").get

    val historyRequest = GetExecutionHistoryRequest
      .builder()
      .executionArn(executionId)
      .reverseOrder(true)
      .build()

    val historyResults =
      stepFunctionsClient.getExecutionHistory(historyRequest).events().asScala
    val lastScheduledEvent =
      historyResults.find(_.`type` == HistoryEventType.TASK_SCHEDULED).head
    val payload = Json.parse(
      lastScheduledEvent.taskScheduledEventDetails().parameters()
    ) \ "Payload"
    val lastSateInput = payload.as[WaitOnUpload]

    // todo: do something useful with `data`, the MediaConvertEvent rather than just resuming the step function
    // probably this will just be adding it to the output of this step.

    val sendTaskSuccessRequest = SendTaskSuccessRequest
      .builder()
      .taskToken(lastSateInput.taskToken)
      .output(Json.stringify(Json.toJson(lastSateInput.input)))
      .build()

    stepFunctionsClient.sendTaskSuccess(sendTaskSuccessRequest)
    "Done"
  }
}
