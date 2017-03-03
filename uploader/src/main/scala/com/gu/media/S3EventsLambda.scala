package com.gu.media

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import scala.collection.JavaConverters._

class S3EventsLambda extends RequestHandler[S3Event, Unit] {
  override def handleRequest(input: S3Event, context: Context): Unit = {
    val records = input.getRecords.asScala

    records.foreach { record =>
      context.getLogger.log(s"Received S3 notification for ${record.getS3.getObject.getKey}")
    }
  }
}
