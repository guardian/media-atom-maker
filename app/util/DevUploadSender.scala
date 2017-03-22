package util

import java.util.concurrent.Executors

import com.gu.media.upload.actions.{UploadAction, UploadActionHandler, UploadActionSender}

class DevUploadSender(handler: UploadActionHandler) extends UploadActionSender {
  private val threadPool = Executors.newSingleThreadScheduledExecutor()

  override def send(action: UploadAction): Unit = {
    threadPool.submit(new Runnable {
      override def run(): Unit = {
        handler.handle(action)
      }
    })
  }
}
