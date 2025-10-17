package com.gu.media.ses

import com.amazonaws.services.simpleemail.model._
import com.gu.media.aws.SESSettings
import com.gu.media.model.MediaAtom
import com.gu.media.Settings

import scala.jdk.CollectionConverters._

class Mailer(config: Settings with SESSettings) {
  def sendPlutoIdMissingEmail(
      atomId: String,
      atomTitle: String,
      sendTo: String
  ): SendEmailResult = {

    val emailBody =
      s"""
        |<div>The video “$atomTitle” that you uploaded via Media Atom Maker recently, does not have a Pluto project associated with it.</div>
        |<div>Please visit ${getLinkTag(
          s"${getAtomUrl(atomId)}/upload",
          "this address"
        )} to set its Pluto project.
        |<div>Without a project, the video won’t be archived and won't maintain a link to its original Adobe Premiere project and assets.</div>
      """.stripMargin

    sendEmail(
      List(sendTo),
      "Failed Pluto Video Ingest - Action Required",
      emailBody
    )
  }

  private def getAtomUrl(atomId: String) =
    s"https://${config.host}/videos/$atomId"

  private def getLinkTag(url: String, text: String) =
    s"""<a href="$url" target="_blank" rel="noopener noreferrer">$text</a>"""

  private def sendEmail(
      recipients: Seq[String],
      subject: String,
      body: String
  ): SendEmailResult = {
    config.sesClient.sendEmail(
      new SendEmailRequest()
        .withDestination(new Destination().withToAddresses(recipients.asJava))
        .withMessage(
          new Message()
            .withSubject(new Content(s"[Media Atom Maker] $subject"))
            .withBody(new Body().withHtml(new Content(body)))
        )
        .withSource(config.fromEmailAddress)
        .withReplyToAddresses(config.replyToAddresses.asJava)
    )
  }

}
