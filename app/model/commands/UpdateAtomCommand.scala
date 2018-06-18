package model.commands

import java.util.Date

import ai.x.diff.DiffShow
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.media.logging.Logging
import data.DataStores
import model.commands.CommandExceptions._
import com.gu.media.model._
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.util.MediaAtomImplicits
import org.joda.time.DateTime
import util.{AWSConfig, YouTube}

import scala.util.{Failure, Success}

case class UpdateAtomCommand(
  id: String,
  upcomingMediaAtom: MediaAtom,
  override val stores: DataStores,
  user: PandaUser,
  awsConfig: AWSConfig,
  youtube: YouTube
) extends Command with MediaAtomImplicits with Logging {
  type T = MediaAtom

  def process(): T = {
    log.info(s"Request to update atom ${upcomingMediaAtom.id}")

    if (id != upcomingMediaAtom.id) {
      AtomIdConflict
    }

    val existingMediaAtom = getExistingMediaAtom()

    val diffString = createDiffString(existingMediaAtom, upcomingMediaAtom)
    log.info(s"Update atom changes ${upcomingMediaAtom.id}: $diffString")

    val thriftAtom = upcomingMediaAtom.copy(
      contentChangeDetails = getContentChangeDetails(existingMediaAtom),
      blockAds = getAtomBlockAds()
    ).asThrift

    previewDataStore.updateAtom(thriftAtom).fold(
      err => {
        log.error(s"Unable to update atom ${upcomingMediaAtom.id}", err)
        AtomUpdateFailed(err.msg)
      },
      _ => {
        val event = ContentAtomEvent(thriftAtom, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => {
            val updatedMediaAtom = MediaAtom.fromThrift(thriftAtom)
            processPlutoData(existingMediaAtom, updatedMediaAtom)
            AuditMessage(upcomingMediaAtom.id, "Update", getUsername(user), Some(diffString)).logMessage()

            updatedMediaAtom
          }
          case Failure(err) =>
            log.error(s"Unable to publish updated atom ${upcomingMediaAtom.id}", err)
            AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
  }

  private def getAtomBlockAds(): Boolean = {
    upcomingMediaAtom.category match {
      // GLabs atoms will always have ads blocked on YouTube,
      // so the thrift field maps to the Composer page and we don't need to check the video duration
      case Category.Hosted | Category.Paid => upcomingMediaAtom.blockAds
      case _ => if (upcomingMediaAtom.duration.getOrElse(0L) < youtube.minDurationForAds) true else upcomingMediaAtom.blockAds
    }
  }

  private def getContentChangeDetails(existingMediaAtom: MediaAtom): ContentChangeDetails = {
    val scheduledLaunchDate: Option[DateTime] = upcomingMediaAtom.contentChangeDetails.scheduledLaunch.map(scheduledLaunch => new DateTime(scheduledLaunch.date))
    val embargo: Option[DateTime] = upcomingMediaAtom.contentChangeDetails.embargo.map(embargo => new DateTime(embargo.date))
    val expiry: Option[DateTime] = upcomingMediaAtom.expiryDate.map(expiry => new DateTime(expiry))

    upcomingMediaAtom.contentChangeDetails.copy(
      revision = existingMediaAtom.contentChangeDetails.revision + 1,
      lastModified = Some(ChangeRecord.now(user)),
      scheduledLaunch = scheduledLaunchDate.map(ChangeRecord.build(_, user)),
      embargo = embargo.map(ChangeRecord.build(_, user)),
      expiry = expiry.map(ChangeRecord.build(_, user))
    )
  }

  private def getExistingMediaAtom(): MediaAtom = {
    val existingAtomAsThrift = getPreviewAtom(upcomingMediaAtom.id)
    MediaAtom.fromThrift(existingAtomAsThrift)
  }

  private def processPlutoData(oldAtom: MediaAtom, newAtom: MediaAtom) = {
    (oldAtom.plutoData.flatMap(_.projectId), newAtom.plutoData.flatMap(_.projectId)) match {
      case (Some(oldProject), Some(newProject)) if oldProject != newProject => notifyPluto(newAtom)
      case (None, Some(_)) => notifyPluto(newAtom)
      case (_, _) => None
    }
  }

  private def notifyPluto(newAtom: MediaAtom) = {
    val plutoActions = new PlutoUploadActions(awsConfig)
    val message = AtomAssignedProjectMessage.build(newAtom)
    plutoActions.sendToPluto(message)
  }

  private val interestingFields = List("title", "category", "description", "duration", "source", "youtubeCategoryId", "license", "commentsEnabled", "channelId", "legallySensitive")

  // We don't use HTTP patch so diffing has to be done manually
  def createDiffString(before: MediaAtom, after: MediaAtom): String = {
    val fieldDiffs = DiffShow.diff[MediaAtom](before, after).string
      .replaceAll("\\[*[0-9]+m", "") // Clean out the silly console coloring stuff
      .split('\n')
      .map(_.trim())
      .filter(line => !line.contains("ERROR")) // More silly stuff from diffing library
      .filter(line => interestingFields.exists(line.contains))
      .mkString(", ")

    if (fieldDiffs == "") { // There's a change, but in some field we're not interested in (or rather, unable to format nicely)
      "Updated atom fields"
    } else {
      s"Updated atom fields ($fieldDiffs)"
    }
  }
}
