package util

import com.gu.media.logging.Logging
import data.{UnpackedDataStores, DataStores}
import akka.actor.Scheduler
import scala.annotation.tailrec
import scala.concurrent.duration._
import play.api.libs.json._
import com.amazonaws.services.sqs.model.{DeleteMessageRequest, ReceiveMessageRequest, Message}
import scala.concurrent.ExecutionContext
import com.gu.scanamo._
import scala.collection.JavaConversions._
import com.amazonaws.services.sns.model.PublishRequest
import model.{VideoUpload, MediaAtom}
import com.gu.media.logging.Logging

case class UploaderMessageConsumer(val stores: DataStores, awsConfig: AWSConfig)
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
      awsConfig.sqsClient.deleteMessage(new DeleteMessageRequest(awsConfig.uploadedQueueUrl, msg.getReceiptHandle()))
    }

    processMessages()

  }

  private def getMessages(waitTime: Int, maxMessages: Int): Seq[Message] =
    awsConfig.sqsClient.receiveMessage(
      new ReceiveMessageRequest(awsConfig.uploadedQueueUrl)
        .withWaitTimeSeconds(waitTime)
        .withMaxNumberOfMessages(maxMessages)
    ).getMessages.toList

  private def handleMessage(msg: Message): Unit = {

    val body = Json.parse(msg.getBody)

    (body \ "Message") match {
      case JsDefined(message) => {
        val messageString = message.toString
        val id = messageString.substring(1, messageString.length - 1)

        val atom = getPreviewAtom(id)
        val mediaAtom = MediaAtom.fromThrift(atom)
        mediaAtom.plutoProjectId match {
          case Some(plutoId) => {
            val req = new PublishRequest(awsConfig.plutoTopicArn, id)
            awsConfig.snsClient.publish(req)
          }
          case None => {
            val videoUpload = VideoUpload(mediaAtom.id, mediaAtom.title, mediaAtom.description.getOrElse(""), None)
            Scanamo.put(awsConfig.dynamoDB)(awsConfig.manualPlutoDynamo)(videoUpload)
          }
        }
      }
      case undefined => log.error(s"Could not extract a message body from message ${msg.getReceiptHandle()}")
    }
  }
}
