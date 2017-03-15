package util

import data.UnpackedDataStores
import akka.actor.Scheduler
import scala.annotation.tailrec
import scala.concurrent.duration._
import play.api.libs.json._
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, ReceiveMessageRequest, Message}
import scala.concurrent.ExecutionContext
import scala.collection.JavaConversions._
import com.gu.media.logging.Logging
import data.DataStores

case class PlutoMessageConsumer(val stores: DataStores, awsConfig: AWSConfig)
  extends UnpackedDataStores with Logging {

  def start(scheduler: Scheduler)(implicit ec: ExecutionContext): Unit = {
    log.info("Starting uploads sqs queue reader")
    //val req = new PublishRequest(awsConfig.plutoTopicArn,
    // "857aebb9-4c99-4d7e-a06d-d88b2a235ac9"
    //)
    //awsConfig.snsClient.publish(req)
    scheduler.scheduleOnce(0.seconds)(processMessages())

  }

  @tailrec
  private def processMessages(): Unit = {

    for (
      msg <- getMessages(waitTime = 20, maxMessages = 1)
    ) yield {
      handleMessage(msg)
      awsConfig.sqsClient.deleteMessage(new DeleteMessageRequest(awsConfig.plutoQueueUrl, msg.getReceiptHandle()))
    }

    processMessages()

  }

  private def getMessages(waitTime: Int, maxMessages: Int): Seq[Message] =
    awsConfig.sqsClient.receiveMessage(
      new ReceiveMessageRequest(awsConfig.plutoQueueUrl)
        .withWaitTimeSeconds(waitTime)
        .withMaxNumberOfMessages(maxMessages)
    ).getMessages.toList

  private def handleMessage(msg: Message): Unit = {

    val body = Json.parse(msg.getBody)

    (body \ "Message") match {
      case JsDefined(message) => {
        val messageString = message.toString
        val atomId = messageString.substring(1, messageString.length - 1)
        //Todo: remove from s3 now that pluto has finished processing
        plutoDataStore.delete(atomId)
      }
      case undefined => log.error(s"Could not extract a message body from message ${msg.getReceiptHandle()}")
    }
  }
}
