package com.gu.media.upload

import com.gu.media.aws.{DynamoAccess, UploadAccess}
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.upload.model.Upload
import org.scanamo.{Scanamo, Table}
import org.scanamo.generic.auto._

class AddUploadDataToCache
    extends LambdaWithParams[Upload, Upload]
    with LambdaBase
    with DynamoAccess
    with UploadAccess {
  private val table = Table[Upload](this.cacheTableName)

  override def handle(input: Upload): Upload = {
    scanamo.exec(table.put(input))
    input
  }
}
