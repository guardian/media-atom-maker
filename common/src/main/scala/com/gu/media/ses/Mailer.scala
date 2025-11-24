package com.gu.media.ses

import software.amazon.awssdk.services.ses.model.{
  Body,
  Content,
  Destination,
  Message,
  SendEmailRequest,
  SendEmailResponse
}
import com.gu.media.aws.SESSettings
import com.gu.media.Settings

import scala.jdk.CollectionConverters._

class Mailer(config: Settings with SESSettings) {
  def sendAtomExpiredEmail(
      atomId: String,
      atomTitle: String,
      sendTo: String
  ): SendEmailResponse = {

    val atomUrl = getAtomUrl(atomId)

    val emailBody =
      s"""
         |<div>A video atom “$atomTitle” has expired.</div>
         |<div>Please review ${getLinkTag(
          atomUrl,
          "the atom’s usages"
        )} and replace it where it’s still in use.</div>
         |""".stripMargin

    sendEmail(
      List(sendTo),
      "Media Atom has expired - Action Required",
      emailBody
    )
  }
  def sendPlutoIdMissingEmail(
      atomId: String,
      atomTitle: String,
      sendTo: String
  ): SendEmailResponse = {

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
  ): SendEmailResponse = {
    val subjectContent = Content.builder().data(subject).build()
    val bodyContent =
      Body.builder().html(Content.builder().data(body).build()).build()
    config.sesClient.sendEmail(
      SendEmailRequest
        .builder()
        .destination(
          Destination.builder().toAddresses(recipients.asJava).build()
        )
        .message(
          Message.builder().subject(subjectContent).body(bodyContent).build()
        )
        .source(config.fromEmailAddress)
        .replyToAddresses(config.replyToAddresses.asJava)
        .build()
    )
  }

}
