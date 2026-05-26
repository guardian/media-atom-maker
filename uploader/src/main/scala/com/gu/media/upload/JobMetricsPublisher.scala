package com.gu.media.upload

import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging

class JobMetricsPublisher
    extends LambdaWithParams[String, String]
    with LambdaBase
    with Logging {

  override def handle(input: String): String = {
    log.info("helloworld")
    "Done"
  }
}
