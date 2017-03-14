package com.gu.media.ses

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model._
import com.gu.media.aws.SESSettings

class SES(sesClient: AmazonSimpleEmailServiceClient, sesSettings: SESSettings) {
  def sendCookieEmail(atomTitle: String, sendTo: String): Unit = {

    val emailBody =
      s"<div>A video with title $atomTitle you uploaded to youtube recently was missing a pluto project id</div>" +
      s"<div>Please visit <a href='www.media-atom-maker.gutools.co.uk/videos/pluto-list'>this address</a> to add a project id."
      s"<div>Without this id Pluto cannot ingest the video</div>"

    sesClient.sendEmail(new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(sendTo))
      .withMessage(new Message()
        .withSubject(new Content("Gutools new cookie link"))
        .withBody(new Body().withHtml(new Content(emailBody)))
      )
      .withSource(sesSettings.fromEmailAddress)
      .withReplyToAddresses(sesSettings.replyToAddresses)
    )

  }
}
