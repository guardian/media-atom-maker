package util

import data.UnpackedDataStores
import org.apache.pekko.actor.{ActorSystem, Scheduler}

import scala.annotation.tailrec
import scala.concurrent.duration._
import software.amazon.awssdk.services.sqs.model.{
  DeleteMessageRequest,
  Message,
  ReceiveMessageRequest
}

import scala.concurrent.ExecutionContext
import com.gu.media.logging.Logging
import data.DataStores

import scala.jdk.CollectionConverters._
import play.api.libs.json._
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

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
        DeleteMessageRequest
          .builder()
          .queueUrl(awsConfig.plutoQueueUrl)
          .receiptHandle(msg.receiptHandle())
          .build()
      )
    }

    processMessages()

  }

  private def getMessages(waitTime: Int, maxMessages: Int): Seq[Message] =
    awsConfig.sqsClient
      .receiveMessage(
        ReceiveMessageRequest
          .builder()
          .queueUrl(awsConfig.plutoQueueUrl)
          .waitTimeSeconds(waitTime)
          .maxNumberOfMessages(maxMessages)
          .build()
      )
      .messages()
      .asScala
      .toList

  private def handleMessage(msg: Message): Unit = {

    val bodyJson = Json.parse(msg.body())

    (bodyJson \ "Message").validate[PlutoMessage] match {
      case JsSuccess(plutoMessage, _) => {
        log.info(
          s"Processing Pluto message to delete asset for s3Key: ${plutoMessage.s3Key}. nb. Deletion is currently disabled so this is a no-op."
        )
      }
      case undefined =>
        log.error(
          s"Could not extract a message body from message ${msg.receiptHandle()}"
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
