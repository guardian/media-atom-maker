package util

import data.UnpackedDataStores
import org.apache.pekko.actor.{ActorSystem, Scheduler}
import scala.annotation.tailrec
import scala.concurrent.duration._
import com.amazonaws.services.sqs.model.{
  DeleteMessageRequest,
  ReceiveMessageRequest,
  Message
}
import scala.concurrent.ExecutionContext
import com.gu.media.logging.Logging
import data.DataStores
import scala.jdk.CollectionConverters._
import play.api.libs.json._

case class PlutoMessageConsumer(val stores: DataStores, awsConfig: AWSConfig)
    extends UnpackedDataStores
    with Logging {

  val actorSystem = ActorSystem("PlutoMessageConsumer")

  def start(scheduler: Scheduler)(implicit ec: ExecutionContext): Unit = {
    log.info(
      s"Starting uploads sqs queue reader for queue ${awsConfig.plutoQueueUrl}"
    )
    actorSystem.scheduler.scheduleOnce(0.seconds)(processMessages())

  }

  @tailrec
  private def processMessages(): Unit = {

    for (msg <- getMessages(waitTime = 20, maxMessages = 1)) yield {
      handleMessage(msg)
      awsConfig.sqsClient.deleteMessage(
        new DeleteMessageRequest(
          awsConfig.plutoQueueUrl,
          msg.getReceiptHandle()
        )
      )
    }

    processMessages()

  }

  private def getMessages(waitTime: Int, maxMessages: Int): Seq[Message] =
    awsConfig.sqsClient
      .receiveMessage(
        new ReceiveMessageRequest(awsConfig.plutoQueueUrl)
          .withWaitTimeSeconds(waitTime)
          .withMaxNumberOfMessages(maxMessages)
      )
      .getMessages
      .asScala
      .toList

  private def handleMessage(msg: Message): Unit = {

    val bodyJson = Json.parse(msg.getBody)

    (bodyJson \ "Message").validate[PlutoMessage] match {
      case JsSuccess(plutoMessage, _) => {

        awsConfig.s3Client.deleteObject(
          awsConfig.userUploadBucket,
          plutoMessage.s3Key
        )

        stores.pluto.get(plutoMessage.s3Key) match {
          case Some(upload) => stores.pluto.delete(plutoMessage.s3Key)
          case _            =>
        }

      }
      case undefined =>
        log.error(
          s"Could not extract a message body from message ${msg.getReceiptHandle()}"
        )
    }
  }
}

case class PlutoMessage(s3Key: String)

object PlutoMessage {

  // Because Pluto Message only has a single field, we cannot use the usual pattern
  // for transforming json because the code does not compile:
  // http://stackoverflow.com/questions/40679540/overloaded-method-value-read-cannot-be-applied-to-string-searchcontroller

  implicit val plutoMessageReads: Reads[PlutoMessage] = (
    ((__)
      .read[String])
      .map(PlutoMessage(_))
  )
}
