package com.gu.media.upload

import com.gu.media.aws.S3Access
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class CreateCompleteVideoInS3 extends LambdaWithParams[Upload, Upload] with S3Access with Logging{
  private val actions = new S3UploadActions(this.s3Client)

  override def handle(upload: Upload): Upload = {
    actions.createCompleteObject(upload, upload.metadata.pluto.s3Key)
    actions.deleteParts(upload)

    upload
  }
}
