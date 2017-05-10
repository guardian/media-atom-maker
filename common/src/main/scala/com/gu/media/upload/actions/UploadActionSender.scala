package com.gu.media.upload.actions

import com.gu.media.aws.KinesisAccess
import com.gu.media.logging.Logging

trait UploadActionSender {
  def send(action: UploadAction): Unit
}

class KinesisActionSender(aws: KinesisAccess) extends UploadActionSender with Logging {
  override def send(action: UploadAction): Unit = {
    log.info(s"Sending action on Kinesis: $action")

    aws.sendOnKinesis(aws.uploadActionsStreamName, action.upload.id, action)
  }
}
