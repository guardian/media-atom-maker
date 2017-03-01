package com.gu.media

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.gu.media.aws.DynamoAccess
import com.gu.media.lambda.LambdaBase
import com.gu.media.upload.{DynamoUploadsTable, UploadEntry, UploadPartKey}

import scala.collection.JavaConverters._

class S3EventsLambda extends RequestHandler[S3Event, Unit] with LambdaBase with DynamoAccess {
  val table = new DynamoUploadsTable(this)

  override def handleRequest(input: S3Event, context: Context): Unit = {
    val log = context.getLogger

    val records = input.getRecords.asScala
    val keys = records.map(_.getS3.getObject.getKey)

    keys.collect {
      case UploadPartKey(_, id, part) =>
        log.log(s"Upload part notification. id=$id part=$part")

        table.get(id) match {
          case Some(before) =>
            val after = completePart(before, part)
            table.put(after)

          case None =>
            log.log(s"ERROR: unknown upload id $id")
        }
    }

    def completePart(upload: UploadEntry, partIndex: Int): UploadEntry = {
      upload.copy(parts = upload.parts.zipWithIndex.map {
        case (part, ix) if ix == partIndex =>
          part.copy(uploadedToS3 = part.end)

        case (part, _) =>
          part
      })
    }
  }
}
