package model.commands

import java.util.Date
import java.util.UUID._

import com.gu.atom.data.{IDConflictError, PreviewDataStore}
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift._
import data.AuditDataStore
import model.{Image, MediaAtom, PrivacyStatus}
import model.commands.CommandExceptions._
import org.cvogt.play.json.Jsonx

import scala.util.{Failure, Success}
import com.gu.contentatom.thrift.{Atom => ThriftAtom}
import com.gu.contentatom.thrift.atom.media.{Category => ThriftCategory, MediaAtom => ThriftMediaAtom, Metadata => ThriftMetadata, PrivacyStatus => ThriftPrivacyStatus}
import com.gu.pandomainauth.model.{User => PandaUser}
import util.Logging

// Since the data store and publisher are injected rather than being objects we cannot serialize JSON directly into a
// command so we'll use a small POD for easy JSONification
case class CreateAtomCommandData(
  title: String,
  category: String, //TODO use Category model for stronger typing
  posterImage: Image,
  duration: Long,
  youtubeCategoryId: String,
  channelId: String,
  privacyStatus: PrivacyStatus,
  expiryDate: Option[Long],
  description: Option[String]
)

object CreateAtomCommandData {
  implicit val createAtomCommandFormat = Jsonx.formatCaseClass[CreateAtomCommandData]
}

case class CreateAtomCommand(data: CreateAtomCommandData)
                            (implicit previewDataStore: PreviewDataStore,
                             previewPublisher: PreviewAtomPublisher,
                             auditDataStore: AuditDataStore,
                             user: PandaUser) extends Command with Logging {
  type T = MediaAtom

  def process() = {
    val atomId = randomUUID().toString
    log.info(s"Request to create new atom $atomId [${data.title}]")

    val atom = ThriftAtom(
      id = atomId,
      atomType = AtomType.Media,
      labels = Nil,
      defaultHtml = "<div></div>", // No content set so empty div
      data = AtomData.Media(ThriftMediaAtom(
        title = data.title,
        assets = Nil,
        activeVersion = None,
        category = ThriftCategory.valueOf(data.category).get,
        plutoProjectId = None,
        source = None,
        posterImage= Some(data.posterImage.asThrift),
        duration = Some(data.duration),
        description = data.description,
        metadata = Some(ThriftMetadata(
          categoryId = Some(data.youtubeCategoryId),
          channelId = Some(data.channelId),
          privacyStatus = data.privacyStatus.asThrift,
          expiryDate = data.expiryDate
        ))
      )),
      contentChangeDetails = ContentChangeDetails(None, None, None, 1L)
    )

    auditDataStore.auditCreate(atom.id, user)

    log.info(s"Creating new atom $atomId [${data.title}]")

    previewDataStore.createAtom(atom).fold({
        case IDConflictError =>
          log.error(s"Cannot create new atom $atomId. The id is already in use")
          AtomIdConflict

        case other =>
          log.error(s"Cannot create new atom $atomId. $other")
          UnknownFailure
      },
      _ => {
        log.info(s"Successfully created new atom $atomId [${data.title}]")

        val event = ContentAtomEvent(atom, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_)  =>
            log.info(s"New atom published to preview $atomId [${data.title}]")
            MediaAtom.fromThrift(atom)

          case Failure(err) =>
            log.error(s"Unable to published new atom to preview $atomId [${data.title}]", err)
            AtomPublishFailed(err.toString)
        }
      }
    )
  }
}
