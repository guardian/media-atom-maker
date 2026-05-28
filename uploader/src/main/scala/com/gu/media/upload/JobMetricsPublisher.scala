package com.gu.media.upload

import com.gu.media.config.Code
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.telemetry.Telemetry
import com.gu.media.upload.model.StepFunctionEvent

import java.net.http.HttpClient

class JobMetricsPublisher
    extends LambdaWithParams[StepFunctionEvent, String]
    with LambdaBase
    with Logging {

  override def handle(input: StepFunctionEvent): String = {
    log.info(input.detail.executionArn)
    val client = HttpClient.newHttpClient()
    val secretArn = sys.env("HMAC_SECRET_ARN")
    log.info(secretArn)
    val telemetry = new Telemetry(Code, secretArn, client)
    telemetry.sendTelemetryEvent("test", Map.empty)
    "Done"
  }
}
