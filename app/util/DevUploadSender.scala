package util

import java.util.concurrent.Executors

import com.gu.media.upload.actions.{UploadAction, UploadActionHandler, UploadActionSender}
import play.api.libs.json.Json

class DevUploadSender(handler: UploadActionHandler) extends UploadActionSender {
  private val threadPool = Executors.newSingleThreadScheduledExecutor()

  override def send(action: UploadAction): Unit = {
    // Serialise to/from JSON, just as we do with Kinesis
    val serialised = Json.stringify(Json.toJson(action))

    threadPool.submit(new Runnable {
      override def run(): Unit = {
        val deserialised = Json.parse(serialised).as[UploadAction]
        handler.handle(deserialised)
      }
    })
  }
}
