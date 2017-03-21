package com.gu.media.ses

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._
import scala.collection.JavaConversions._

class Mailer(sesClient: AmazonSimpleEmailServiceClient) {
  def sendPlutoIdMissingEmail(atomTitle: String, sendTo: String, fromAddress: String, replyToAddresses: Array[String]): Unit = {

    val emailBody =
      s"<div>A video with title $atomTitle you uploaded to youtube recently was missing a pluto project id</div>" +
      s"<div>Please visit <a href='www.media-atom-maker.gutools.co.uk/videos/pluto-list'>this address</a> to add a project id."
      s"<div>Without this id Pluto cannot ingest the video</div>"

    sesClient.sendEmail(new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(sendTo))
      .withMessage(new Message()
        .withSubject(new Content("Failed Pluto Video Ingest - Action Required"))
        .withBody(new Body().withHtml(new Content(emailBody)))
      )
      .withSource(fromAddress)
      .withReplyToAddresses(replyToAddresses.toSeq)
    )

  }
}
