package com.gu.media.upload

import com.gu.media.aws.StepFunctionsAccess
import com.gu.media.config.Code
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.telemetry.Telemetry
import com.gu.media.upload.model.StepFunctionEvent
import com.gu.media.util.JobCompletedMetrics

import java.net.http.HttpClient

class JobMetricsPublisher
    extends LambdaWithParams[StepFunctionEvent, String]
    with LambdaBase
    with Logging
    with StepFunctionsAccess {

  override def handle(input: StepFunctionEvent): String = {
    log.info(
      s"Executing jobs metrics publisher lambda after ${input.detail.executionArn} has finished"
    )
    val client = HttpClient.newHttpClient()
    val secretArn = sys.env("HMAC_SECRET_ARN")
    val telemetry = new Telemetry(Code, secretArn, client)
    val sfMetrics = new JobCompletedMetrics(stepFunctionsClient)
    val metrics = sfMetrics.getMetricsForJobRun(input.detail.executionArn)
    log.info("Sending telemetry event")
    telemetry.sendTelemetryEvent("VIDEO_UPLOAD_COMPLETE", metrics)
    log.info("Completed jobs metrics publisher lambda")
    "Done"
  }
}
