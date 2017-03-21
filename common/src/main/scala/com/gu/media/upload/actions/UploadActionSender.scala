package com.gu.media.upload.actions

import com.gu.media.aws.KinesisAccess

trait UploadActionSender {
  def send(action: UploadAction): Unit
}

class KinesisActionSender(aws: KinesisAccess) extends UploadActionSender {
  override def send(action: UploadAction): Unit = {
    aws.sendOnKinesis(aws.uploadActionsStreamName, action.uploadId, action)
  }
}

class LocalActionSender(handler: UploadActionHandler) extends UploadActionSender {
  override def send(action: UploadAction): Unit = {
    handler.handle(action)
  }
}
