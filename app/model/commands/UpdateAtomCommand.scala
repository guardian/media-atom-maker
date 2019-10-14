package model.commands

import java.util.Date

import ai.x.diff.DiffShow
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.media.logging.Logging
import data.DataStores
import model.commands.CommandExceptions._
import com.gu.media.model.{AtomAssignedProjectMessage, ChangeRecord, MediaAtom, AuditMessage}
import com.gu.contentatom.thrift.{ChangeRecord => ThriftChangeRecord}
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.util.MediaAtomImplicits
import org.joda.time.DateTime
import util.AWSConfig

import scala.util.{Failure, Success}

case class UpdateAtomCommand(id: String, atom: MediaAtom, override val stores: DataStores, user: PandaUser, awsConfig: AWSConfig)
    extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def getDateIfNotPublished(dateRecord: Option[ChangeRecord], published: Option[ThriftChangeRecord]): Option[DateTime] =
    published match {
      case Some(_) => None
      case None => dateRecord.map(_.date)
    }

  def process(): T = {
    log.info(s"Request to update atom ${atom.id}")

    if (id != atom.id) {
      AtomIdConflict
    }

    val existingAtom = getPreviewAtom(atom.id)

    val diffString = createDiffString(MediaAtom.fromThrift(existingAtom), atom)
    log.info(s"Update atom changes ${atom.id}: $diffString")

    val changeRecord = ChangeRecord.now(user)
    val atomIsPublished = existingAtom.contentChangeDetails.published

    val scheduledLaunchDate: Option[DateTime] = getDateIfNotPublished(atom.contentChangeDetails.scheduledLaunch, atomIsPublished)

    val embargo: Option[DateTime] = getDateIfNotPublished(atom.contentChangeDetails.embargo, atomIsPublished)

    val expiry: Option[DateTime] = atom.expiryDate.map(expiry => new DateTime(expiry))

    val details = atom.contentChangeDetails.copy(
      revision = existingAtom.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord),
      scheduledLaunch = scheduledLaunchDate.map(ChangeRecord.build(_, user)),
      embargo = embargo.map(ChangeRecord.build(_, user)),
      expiry = expiry.map(ChangeRecord.build(_, user))
    )

    val newAtom = atom.copy(contentChangeDetails = details)
    val thrift = newAtom.asThrift

    previewDataStore.updateAtom(thrift).fold(
      err => {
        log.error(s"Unable to update atom id=${atom.id} in ${awsConfig.dynamoTableName} new_content=$newAtom", err)
        AtomUpdateFailed(err.msg)
      },
      _ => {
        val event = ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_) => {

            val existingMediaAtom = MediaAtom.fromThrift(existingAtom)
            val updatedMediaAtom = MediaAtom.fromThrift(thrift)
            processPlutoData(existingMediaAtom, updatedMediaAtom)

            AuditMessage(atom.id, "Update", getUsername(user), Some(diffString)).logMessage()

            log.info(s"atom ${atom.id} updated successfully in ${awsConfig.dynamoTableName} to $updatedMediaAtom")

            updatedMediaAtom
          }
          case Failure(err) =>
            log.error(s"Unable to publish updated atom id=${atom.id} new_content=$newAtom", err)
            AtomPublishFailed(s"could not publish: ${err.toString}")
        }
      }
    )
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
