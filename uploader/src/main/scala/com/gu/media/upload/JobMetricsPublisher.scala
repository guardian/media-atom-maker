package com.gu.media.upload

<<<<<<< HEAD
import com.gu.media.aws.{SecretsManagerAccess, StepFunctionsAccess}
import com.gu.media.config.Code
=======
import com.gu.media.aws.StepFunctionsAccess
import com.gu.media.config.Stage
>>>>>>> ld / run -metrics
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.telemetry.{HMACClient, Telemetry}
import com.gu.media.upload.model.StepFunctionEvent
import com.gu.media.util.JobCompletedMetrics

import java.net.http.HttpClient

class JobMetricsPublisher
    extends LambdaWithParams[StepFunctionEvent, String]
    with LambdaBase
    with Logging
    with StepFunctionsAccess
    with SecretsManagerAccess {

  override def handle(input: StepFunctionEvent): String = {
    log.info(
      s"Executing jobs metrics publisher lambda after ${input.detail.executionArn} has finished"
    )
    val client = HttpClient.newHttpClient()
    val secretArn = sys.env("HMAC_SECRET_ARN")
    val secret = getSecret(secretArn) getOrElse (throw new Exception(
      s"Could not retrieve $secretArn from secrets manager"
    ))
    val hmacClient = new HMACClient(secret)
    val telemetry = new Telemetry(Stage(stage), hmacClient, client)
    val sfMetrics = new JobCompletedMetrics(stepFunctionsClient)
    val metrics = sfMetrics.getMetricsForJobRun(input)
    log.info("Sending telemetry event")
    telemetry.sendTelemetryEvent("VIDEO_UPLOAD_COMPLETE", metrics)
    log.info("Completed jobs metrics publisher lambda")
    "Done"
  }
}
