package model.commands

import java.util.Date
import com.gu.atom.data.{AtomSerializer, VersionConflictError}
import com.gu.contentatom.thrift.{
  Atom,
  ContentAtomEvent,
  EventType,
  ChangeRecord => ThriftChangeRecord
}
import com.gu.media.logging.Logging
import com.gu.media.model.{
  AtomAssignedProjectMessage,
  AuditMessage,
  ChangeRecord,
  MediaAtom
}
import com.gu.media.upload.PlutoUploadActions
import com.gu.media.util.MediaAtomImplicits
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.commands.UpdateAtomCommand.createDiffString
import org.joda.time.DateTime
import util.AWSConfig

import scala.util.{Failure, Success}

case class UpdateAtomCommand(
    id: String,
    atom: MediaAtom,
    override val stores: DataStores,
    user: PandaUser,
    awsConfig: AWSConfig
) extends Command
    with MediaAtomImplicits
    with Logging {

  type T = MediaAtom

  def getDateIfNotPublished(
      dateRecord: Option[ChangeRecord],
      published: Option[ThriftChangeRecord]
  ): Option[DateTime] =
    published match {
      case Some(_) => None
      case None    => dateRecord.map(_.date)
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

    val scheduledLaunchDate: Option[DateTime] = getDateIfNotPublished(
      atom.contentChangeDetails.scheduledLaunch,
      atomIsPublished
    )

    val embargo: Option[DateTime] =
      getDateIfNotPublished(atom.contentChangeDetails.embargo, atomIsPublished)

    val expiry: Option[DateTime] =
      atom.expiryDate.map(expiry => new DateTime(expiry))

    def updateIfChanged(
        newDate: Option[DateTime],
        changeRecord: Option[ThriftChangeRecord]
    ): Option[ChangeRecord] = {
      if (changeRecord.map(_.date) == newDate.map(_.getMillis)) {
        changeRecord.map(ChangeRecord.fromThrift)
      } else {
        newDate.map(ChangeRecord.build(_, user))
      }
    }

    val existingChangeDetails = existingAtom.contentChangeDetails

    val details = atom.contentChangeDetails.copy(
      revision = atom.contentChangeDetails.revision + 1,
      lastModified = Some(changeRecord),
      scheduledLaunch = updateIfChanged(
        scheduledLaunchDate,
        existingChangeDetails.scheduledLaunch
      ),
      embargo = updateIfChanged(embargo, existingChangeDetails.embargo),
      expiry = updateIfChanged(expiry, existingChangeDetails.expiry)
    )

    val newAtom = atom.copy(contentChangeDetails = details)
    val thrift: Atom = newAtom.asThrift
    val newAtomAsJson = AtomSerializer.toJson(thrift)

    log.info(
      s"Attempting to update atom ${atom.id} in ${awsConfig.dynamoTableName} to new atom: $newAtomAsJson"
    )

    previewDataStore
      .updateAtom(thrift)
      .fold(
        {
          case err: VersionConflictError =>
            log.warn(
              s"Unable to update atom due to version conflict with id ${atom.id} in ${awsConfig.dynamoTableName} table to new content: $newAtomAsJson",
              err
            )
            AtomUpdateConflictError(err.msg)
          case err =>
            log.error(
              s"Unable to update atom with id ${atom.id} in ${awsConfig.dynamoTableName} table to new content: $newAtomAsJson",
              err
            )
            AtomUpdateFailed(err.msg)
        },
        _ => {
          val event =
            ContentAtomEvent(thrift, EventType.Update, new Date().getTime)

          previewPublisher.publishAtomEvent(event) match {
            case Success(_) => {

              val existingMediaAtom = MediaAtom.fromThrift(existingAtom)
              val updatedMediaAtom = MediaAtom.fromThrift(thrift)
              processPlutoData(existingMediaAtom, updatedMediaAtom)

              AuditMessage(
                atom.id,
                "Update",
                getUsername(user),
                Some(diffString)
              ).logMessage()

              log.info(
                s"atom with id ${atom.id} updated successfully in ${awsConfig.dynamoTableName} table to new content: $newAtomAsJson"
              )

              updatedMediaAtom
            }
            case Failure(err) =>
              log.error(
                s"Unable to publish updated atom id=${atom.id} new_content=$newAtom",
                err
              )
              AtomPublishFailed(s"could not publish: ${err.toString}")
          }
        }
      )
  }

  private def processPlutoData(oldAtom: MediaAtom, newAtom: MediaAtom) = {
    (
      oldAtom.plutoData.flatMap(_.projectId),
      newAtom.plutoData.flatMap(_.projectId)
    ) match {
      case (Some(oldProject), Some(newProject)) if oldProject != newProject =>
        notifyPluto(newAtom)
      case (None, Some(_)) => notifyPluto(newAtom)
      case (_, _)          => None
    }
  }

  private def notifyPluto(newAtom: MediaAtom) = {
    val plutoActions = new PlutoUploadActions(awsConfig)
    val message = AtomAssignedProjectMessage.build(newAtom)
    plutoActions.sendToPluto(message)
  }
}

object UpdateAtomCommand {
  private val interestingFields = Seq[(String, MediaAtom => Option[Any])](
    ("title", a => Some(a.title)),
    ("category", a => Some(a.category)),
    ("description", _.description),
    ("duration", _.duration),
    ("source", _.source),
    ("youtubeCategoryId", _.youtubeCategoryId),
    ("license", _.license),
    ("commentsEnabled", _.composerCommentsEnabled),
    ("channelId", _.channelId),
    ("legallySensitive", _.legallySensitive)
  )

  // We don't use HTTP patch so diffing has to be done manually
  def createDiffString(before: MediaAtom, after: MediaAtom): String = {
    val changedFields = for {
      (name, extractor) <- interestingFields
      distinctValues = Seq(extractor(before), extractor(after)).distinct
      if distinctValues.size > 1
    } yield s"$name: ${distinctValues.map(_.getOrElse("[NONE]")).mkString(" -> ")}"

    if (changedFields.isEmpty) { // There's a change, but in some field we're not interested in (or rather, unable to format nicely)
      "Updated atom fields"
    } else s"Updated atom fields (${changedFields.mkString(", ")})"
  }
}
