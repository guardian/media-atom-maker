package com.gu.media.upload

import com.gu.media.aws.ElasticTranscodeAccess
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.upload.model.Upload

class SendToTranscoder extends LambdaWithParams[Upload, Upload]
  with ElasticTranscodeAccess
  with Logging
{
  private val transcoder = new TranscodeUploadActions(this.transcoderClient)

  override def handle(upload: Upload): Upload = {
    transcoder.transcodeToS3(upload.metadata.pluto.s3Key, this.transcodePipelineId)
    upload
  }
}
