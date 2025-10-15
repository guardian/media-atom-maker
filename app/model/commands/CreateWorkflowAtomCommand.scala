package model.commands

import java.util.Date
import java.util.UUID._

import com.gu.atom.data.IDConflictError
import com.gu.contentatom.thrift.atom.media.{
  Category => ThriftCategory,
  MediaAtom => ThriftMediaAtom,
  Metadata => ThriftMetadata
}
import com.gu.contentatom.thrift.{
  AtomData,
  AtomType,
  ContentAtomEvent,
  ContentChangeDetails,
  EventType,
  Atom => ThriftAtom
}
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.WorkflowMediaAtom
import com.gu.media.model.{ChangeRecord, MediaAtom, AuditMessage}

import scala.util.{Failure, Success}

case class CreateWorkflowAtomCommand(
    workflowMediaAtom: WorkflowMediaAtom,
    override val stores: DataStores,
    user: PandaUser
) extends Command
    with Logging {

  type T = MediaAtom

  def process(): MediaAtom = {
    val atomId = randomUUID().toString
    log.info(
      s"Request to create new atom $atomId [${workflowMediaAtom.title}] from Workflow"
    )

    val createdChangeRecord = Some(ChangeRecord.now(user).asThrift)

    val atom = ThriftAtom(
      id = atomId,
      atomType = AtomType.Media,
      labels = Nil,
      defaultHtml = "<div></div>", // No content set so empty div
      data = AtomData.Media(
        ThriftMediaAtom(
          title = workflowMediaAtom.title,
          assets = Nil,
          activeVersion = None,
          category = ThriftCategory.News,
          plutoProjectId = None,
          source = None,
          posterImage = None,
          duration = None,
          description = None,
          metadata = Some(
            ThriftMetadata(
              categoryId = None,
              channelId = None,
              privacyStatus = None,
              expiryDate = None
            )
          )
        )
      ),
      contentChangeDetails = ContentChangeDetails(
        lastModified = createdChangeRecord,
        created = createdChangeRecord,
        published = None,
        revision = 1L
      )
    )

    AuditMessage(atomId, "Workflow Create", getUsername(user)).logMessage()

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
          log.info(
            s"Successfully created new atom $atomId [${workflowMediaAtom.title}]"
          )

          val event =
            ContentAtomEvent(atom, EventType.Update, new Date().getTime)

          previewPublisher.publishAtomEvent(event) match {
            case Success(_) =>
              log.info(
                s"New atom published to preview $atomId [${workflowMediaAtom.title}]"
              )
              MediaAtom.fromThrift(atom)

            case Failure(err) =>
              log.error(
                s"Unable to published new atom to preview $atomId [${workflowMediaAtom.title}]",
                err
              )
              AtomPublishFailed(err.toString)
          }
        }
      )
  }
}
