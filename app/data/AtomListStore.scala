package data

import java.time.Instant

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.media.CapiAccess
import com.gu.media.model.{Image, MediaAtom}
import com.gu.media.util.TestFilters
import model.commands.CommandExceptions.AtomDataStoreError
import model.{MediaAtomList, MediaAtomSummary}
import play.api.libs.json.{JsArray, JsValue}

trait AtomListStore {
  def getAtoms(search: Option[String], limit: Option[Int]): MediaAtomList
}

class CapiBackedAtomListStore(capi: CapiAccess) extends AtomListStore {
  override def getAtoms(search: Option[String], limit: Option[Int]): MediaAtomList = {
    // CAPI max page size is 200
    val cappedLimit = limit.map(Math.min(200, _))

    val base = "atoms?types=media&order-by=newest"
    val searchPart = search.map { q => s"&searchFields=data.title&q=$q" }.getOrElse("")
    val pageSizePart = cappedLimit.map { l => s"&page-size=$l" }.getOrElse("")

    val query = base + searchPart + pageSizePart
    val response = capi.capiQuery(query)

    val total = (response \ "response" \ "total").as[Int]
    val results = (response \ "response" \ "results").as[JsArray]

    MediaAtomList(total, results.value.flatMap(fromJson).toList)
  }

  private def fromJson(wrapper: JsValue): Option[MediaAtomSummary] = {
    val id = (wrapper \ "id").as[String]
    val atom = wrapper \ "data" \ "media"

    val category = (atom \ "category").as[String]

    val title = (atom \ "title").as[String]

    if (title.startsWith(TestFilters.testAtomBaseName)) None /* This filters out test atoms created by the Integration Tests, as we don't want them to be user facing */
    else {
      val posterImage = (atom \ "posterImage").asOpt[Image]

      val expiryDate = (atom \ "expiryDate").asOpt[Long]
      val activeVersion = (atom \ "activeVersion").asOpt[Long]

      val versions = (atom \ "assets").as[JsArray].value.map { asset =>
        (asset \ "version").as[Long]
      }

      val state = AtomListStore.getState(expiryDate, activeVersion, versions.toSet)

      Some(MediaAtomSummary(id, state, title, posterImage))
    }
  }
}

class DynamoBackedAtomListStore(store: PreviewDynamoDataStore) extends AtomListStore {
  override def getAtoms(search: Option[String], limit: Option[Int]): MediaAtomList = {
    // We must filter the entire list of atoms rather than use Dynamo limit to ensure stable iteration order.
    // Without it, the front page will shuffle around when clicking the Load More button.
    store.listAtoms match {
      case Left(err) =>
        AtomDataStoreError(err.msg)

      case Right(atoms) =>
        def created(atom: MediaAtom) = atom.contentChangeDetails.created.map(_.date.getMillis)

        val mediaAtoms = atoms
          .map(MediaAtom.fromThrift)
          .toList
          .sortBy(created)
          .reverse // newest atoms first

        val filteredAtoms = search match {
          case Some(str) => mediaAtoms.filter(_.title.contains(str))
          case None => mediaAtoms
        }

        val limitedAtoms = limit match {
          case Some(l) => filteredAtoms.take(l)
          case None => filteredAtoms
        }

        MediaAtomList(filteredAtoms.size, limitedAtoms.map(fromAtom))
    }
  }

  private def fromAtom(atom: MediaAtom): MediaAtomSummary = {
    val versions = atom.assets.map(_.version).toSet
    val state = AtomListStore.getState(atom.expiryDate, atom.activeVersion, versions)

    MediaAtomSummary(atom.id, state, atom.title, atom.posterImage)
  }
}

object AtomListStore {

  def apply(stage: String, capi: CapiAccess, store: PreviewDynamoDataStore): AtomListStore = stage match {
    case "DEV" => new DynamoBackedAtomListStore(store)
    case _ => new CapiBackedAtomListStore(capi)
  }

  def getState(expiryDate: Option[Long], activeVersion: Option[Long], versions: Set[Long]): String = {
    val now = Instant.now().toEpochMilli
    val hasExpired = expiryDate.exists(_ < now)

    val hasVideo = activeVersion match {
      case Some(version) => versions.contains(version)
      case None => false
    }

    if(hasExpired) {
      "Expired"
    } else if(hasVideo) {
      "Active"
    } else {
      "No Video"
    }
  }
}
