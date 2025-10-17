package model.commands

import java.util.Date
import org.joda.time.DateTime
import java.util.UUID._

import com.gu.atom.data.IDConflictError
import com.gu.contentatom.thrift.{ContentAtomEvent, EventType}
import com.gu.media
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.media.model.{ChangeRecord, MediaAtom, MediaAtomBeforeCreation}
import com.gu.media.model.AuditMessage

import scala.util.{Failure, Success}

case class CreateAtomCommand(
    data: MediaAtomBeforeCreation,
    override val stores: DataStores,
    user: PandaUser
) extends Command
    with Logging {

  type T = MediaAtom

  def process() = {
    val atomId = randomUUID().toString
    val createdChangeRecord = Some(ChangeRecord.now(user))
    val scheduledLaunchDate: Option[DateTime] =
      data.contentChangeDetails.scheduledLaunch.map(scheduledLaunch =>
        new DateTime(scheduledLaunch.date)
      )
    val embargo: Option[DateTime] =
      data.contentChangeDetails.embargo.map(embargo =>
        new DateTime(embargo.date)
      )
    val expiry: Option[DateTime] =
      data.expiryDate.map(expiry => new DateTime(expiry))
    val details = media.model.ContentChangeDetails(
      lastModified = createdChangeRecord,
      created = createdChangeRecord,
      published = None,
      revision = 1L,
      scheduledLaunch = scheduledLaunchDate.map(ChangeRecord.build(_, user)),
      embargo = embargo.map(ChangeRecord.build(_, user)),
      expiry = expiry.map(ChangeRecord.build(_, user))
    )

    log.info(s"Request to create new atom $atomId [${data.title}]")

    val atom = data.asThrift(atomId, details)

    AuditMessage(atom.id, "Create", getUsername(user)).logMessage()

    previewDataStore
      .createAtom(atom)
      .fold(
        {
          case IDConflictError =>
            log.error(
              s"Cannot create new atom $atomId. The id is already in use"
            )
            AtomIdConflict

          case other =>
            log.error(s"Cannot create new atom $atomId. $other")
            UnknownFailure
        },
        _ => {
          log.info(s"Successfully created new atom $atomId [${data.title}]")

          val event =
            ContentAtomEvent(atom, EventType.Update, new Date().getTime)

          previewPublisher.publishAtomEvent(event) match {
            case Success(_) =>
              log.info(s"New atom published to preview $atomId [${data.title}]")
              MediaAtom.fromThrift(atom)

            case Failure(err) =>
              log.error(
                s"Unable to published new atom to preview $atomId [${data.title}]",
                err
              )
              AtomPublishFailed(err.toString)
          }
        }
      )
  }
}
