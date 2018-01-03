package com.gu.media.ses

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._

import scala.collection.JavaConversions._

class Mailer(sesClient: AmazonSimpleEmailServiceClient, host: String) {
  def sendPlutoIdMissingEmail(atomId: String, atomTitle: String, sendTo: String, fromAddress: String, replyToAddresses: Array[String]): Unit = {

    val emailBody =
      s"""
        |<div>The video “$atomTitle” that you uploaded via Media Atom Maker recently, does not have a Pluto project associated with it.</div>
        |<div>Please visit <a href="https://$host/videos/$atomId/upload" target="_blank" rel="noopener noreferrer">this address</a> to set its Pluto project.
        |<div>Without a project, the video won’t be archived and won't maintain a link to its original Adobe Premiere project and assets.</div>
        |
        |<div>For a complete list of incomplete Atoms, <a href="https://$host/videos/pluto-list" target="_blank" rel="noopener noreferrer">click here</a>.
      """.stripMargin

    sesClient.sendEmail(new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(sendTo))
      .withMessage(new Message()
        .withSubject(new Content("[Media Atom Maker] Failed Pluto Video Ingest - Action Required"))
        .withBody(new Body().withHtml(new Content(emailBody)))
      )
      .withSource(fromAddress)
      .withReplyToAddresses(replyToAddresses.toSeq)

    )
  }
}
