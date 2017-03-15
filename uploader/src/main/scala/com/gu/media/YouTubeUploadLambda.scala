package com.gu.media

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.gu.media.logging.Logging

class YouTubeUploadLambda extends RequestHandler[KinesisEvent, Unit] with Logging {
  override def handleRequest(input: KinesisEvent, context: Context): Unit = {
    val records = input.getRecords

    if(records.size() > 1) {
      log.error(s"Expected 1 record in each batch, got ${records.size()}. The extra records will be discarded")
    } else {
      val record = input.getRecords.get(0)
      val data = new String(record.getKinesis.getData.array(), "UTF-8")

      log.info(s"Kinesis trigger: $data")
    }
  }
}
