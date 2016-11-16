package model.commands

import java.util.Date
import java.util.UUID._

import com.gu.atom.data.{PreviewDataStore, IDConflictError}
import com.gu.atom.publish.PreviewAtomPublisher
import com.gu.contentatom.thrift._
import model.MediaAtom
import model.commands.CommandExceptions._
import org.cvogt.play.json.Jsonx

import scala.util.{Failure, Success}

import com.gu.contentatom.thrift.{Atom => ThriftAtom}

import com.gu.contentatom.thrift.atom.media.{MediaAtom => ThriftMediaAtom, Category => ThriftCategory}

// Since the data store and publisher are injected rather than being objects we cannot serialize JSON directly into a
// command so we'll use a small POD for easy JSONification
case class CreateAtomCommandData(title: String, category: String, posterUrl: String, duration: Long)

object CreateAtomCommandData {
  implicit val createAtomCommandFormat = Jsonx.formatCaseClass[CreateAtomCommandData]
}

case class CreateAtomCommand(data: CreateAtomCommandData)
                            (implicit previewDataStore: PreviewDataStore,
                             previewPublisher: PreviewAtomPublisher) extends Command {
  type T = MediaAtom

  def process() = {

    val atom = ThriftAtom(
      id = randomUUID().toString,
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
        posterUrl = Some(data.posterUrl),
        duration = Some(data.duration),
        description = None,
        metadata = None
      )),
      contentChangeDetails = ContentChangeDetails(None, None, None, 1L)
    )

    previewDataStore.createAtom(atom).fold({
        case IDConflictError => AtomIdConflict
        case _ => UnknownFailure
      },
      _ => {
        val event = ContentAtomEvent(atom, EventType.Update, new Date().getTime)

        previewPublisher.publishAtomEvent(event) match {
          case Success(_)  => MediaAtom.fromThrift(atom)
          case Failure(err) => AtomPublishFailed(err.toString)
        }
      }
    )
  }
}
