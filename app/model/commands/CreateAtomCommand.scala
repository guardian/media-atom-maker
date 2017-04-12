package model.commands

import java.util.Date
import java.util.UUID._

import com.gu.atom.data.IDConflictError
import com.gu.contentatom.thrift.atom.media.{Category => ThriftCategory, MediaAtom => ThriftMediaAtom, Metadata => ThriftMetadata}
import com.gu.contentatom.thrift.{Atom => ThriftAtom, _}
import com.gu.media.AuditEvents
import com.gu.media.logging.Logging
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.CommandExceptions._
import model.{Audit, ChangeRecord, Image, MediaAtom, PrivacyStatus}
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.util.{Failure, Success}

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
  implicit val createAtomCommandFormat: Format[CreateAtomCommandData] = Jsonx.formatCaseClass[CreateAtomCommandData]
}

case class CreateAtomCommand(data: CreateAtomCommandData, override val stores: DataStores, user: PandaUser)

  extends Command with Logging {

  type T = MediaAtom

  def process(): (MediaAtom, Audit) = {
    val atomId = randomUUID().toString
    log.info(s"Request to create new atom $atomId [${data.title}]")

    val createdChangeRecord = Some(ChangeRecord.now(user).asThrift)

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
      contentChangeDetails = ContentChangeDetails(
        lastModified = createdChangeRecord,
        created = createdChangeRecord,
        published = None,
        revision = 1L
      )
    )

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

        val capiEvent = ContentAtomEvent(atom, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(capiEvent) match {
          case Success(_)  =>
            log.info(s"New atom published to preview $atomId [${data.title}]")

            val mediaAtom = MediaAtom.fromThrift(atom)
            val auditEvent = Audit(atom.id, AuditEvents.CREATE, MediaAtom.fromThrift(atom), user)

            (mediaAtom, auditEvent)

          case Failure(err) =>
            log.error(s"Unable to published new atom to preview $atomId [${data.title}]", err)
            AtomPublishFailed(err.toString)
        }
      }
    )
  }
}
