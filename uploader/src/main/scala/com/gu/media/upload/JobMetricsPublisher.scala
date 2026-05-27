package com.gu.media.upload

import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.upload.model.StepFunctionEvent

class JobMetricsPublisher
    extends LambdaWithParams[StepFunctionEvent, String]
    with LambdaBase
    with Logging {

  override def handle(input: StepFunctionEvent): String = {
    log.info(input.detail.executionArn)
    "Done"
  }
}
