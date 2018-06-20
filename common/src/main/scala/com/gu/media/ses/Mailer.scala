package com.gu.media.ses

import com.amazonaws.services.simpleemail.model._
import com.gu.media.aws.SESSettings
import com.gu.media.model.MediaAtom
import com.gu.media.Settings

import scala.collection.JavaConversions._

class Mailer(config: Settings with SESSettings) {
  def sendPlutoIdMissingEmail(atomId: String, atomTitle: String, sendTo: String): SendEmailResult = {

    val emailBody =
      s"""
        |<div>The video “$atomTitle” that you uploaded via Media Atom Maker recently, does not have a Pluto project associated with it.</div>
        |<div>Please visit ${getLinkTag(s"${getAtomUrl(atomId)}/upload", "this address")} to set its Pluto project.
        |<div>Without a project, the video won’t be archived and won't maintain a link to its original Adobe Premiere project and assets.</div>
        |
        |<div>For a complete list of incomplete Atoms, ${getLinkTag(s"https://${config.host}/videos/pluto-list", "click here")}.
      """.stripMargin

    sendEmail(List(sendTo), "Failed Pluto Video Ingest - Action Required", emailBody)
  }

  def sendWorldCupEmail(atom: MediaAtom): Option[SendEmailResult] = {
    atom.getActiveYouTubeAsset() match {
      case Some(asset) => {
        val emailBody =
          s"""
             | <div>A Video Atom has been published with a YouTube video with either a tag <code>2018 world cup</code> or <code>world cup 2018</code>.</div>
             | <div>Click ${getLinkTag(getAtomUrl(atom.id), "here")} to view the Atom.</div>
             | <div>Click ${getLinkTag(getActiveYoutubeAssetLink(asset.id), "here")} to view the YouTube video</div>
           """.stripMargin

        Some(sendEmail(config.worldCupEmailRecipients, "Atom published with World Cup 2018 tags", emailBody))
      }
      case _ => None
    }
  }

  private def getAtomUrl(atomId: String) = s"https://${config.host}/videos/$atomId"

  private def getActiveYoutubeAssetLink(youtubeVideoId: String) = s"https://www.youtube.com/watch?v=$youtubeVideoId"

  private def getLinkTag(url: String, text: String) = s"""<a href="$url" target="_blank" rel="noopener noreferrer">$text</a>"""

  private def sendEmail(recipients: Seq[String], subject: String, body: String): SendEmailResult = {
    config.sesClient.sendEmail(new SendEmailRequest()
      .withDestination(new Destination().withToAddresses(recipients))
      .withMessage(new Message()
        .withSubject(new Content(s"[Media Atom Maker] $subject"))
        .withBody(new Body().withHtml(new Content(body)))
      )
      .withSource(config.fromEmailAddress)
      .withReplyToAddresses(config.replyToAddresses)
    )
  }

}
