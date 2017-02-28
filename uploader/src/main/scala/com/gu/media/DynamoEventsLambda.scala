package com.gu.media

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import scala.collection.JavaConverters._

class DynamoEventsLambda extends RequestHandler[DynamodbEvent, Unit] {
  override def handleRequest(input: DynamodbEvent, context: Context): Unit = {
    val records = input.getRecords.asScala
    val log = context.getLogger

    records.foreach { record =>
      log.log("Received Dynamo notification:")

      val image = record.getDynamodb.getNewImage.asScala
      image.foreach { case(k, v) =>
        log.log(s"\t$k: $v")
      }
    }
  }
}
