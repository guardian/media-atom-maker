package data

import java.time.Instant

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.media.CapiAccess
import com.gu.media.model.{Image, MediaAtom, ContentChangeDetails}
import com.gu.media.util.TestFilters
import model.commands.CommandExceptions.AtomDataStoreError
import model.{MediaAtomList, MediaAtomSummary}
import play.api.libs.json.{JsArray, JsValue}

trait AtomListStore {
  def getAtoms(search: Option[String], limit: Option[Int], shouldUseCreatedDateForSort: Boolean): MediaAtomList
}

class CapiBackedAtomListStore(capi: CapiAccess) extends AtomListStore {
  override def getAtoms(search: Option[String], limit: Option[Int], shouldUseCreatedDateForSort: Boolean): MediaAtomList = {
    // CAPI max page size is 200
    val cappedLimit: Option[Int] = limit.map(Math.min(200, _))

    val base: Map[String, String] = Map(
      "types" -> "media",
      "order-by" -> "newest"
    ) ++ (if(shouldUseCreatedDateForSort) Map("order-date" -> "first-publication") else Map.empty)

    val baseWithSearch = search match {
      case Some(q) => base ++ Map(
        "q" -> q,
        "searchFields" -> "title"
      )
      case None => base
    }

    val baseWithSearchAndLimit = cappedLimit match {
      case Some(pageSize) => baseWithSearch ++ Map(
        "page-size" -> pageSize.toString
      )
      case None => baseWithSearch
    }

    val response = capi.capiQuery("atoms", baseWithSearchAndLimit)

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

      val activeVersion = (atom \ "activeVersion").asOpt[Long]


      val contentChangeDetails = (wrapper \ "contentChangeDetails").as[ContentChangeDetails]

      val expiryDate = (wrapper \ "contentChangeDetails" \ "expiry" \ "date").asOpt[Long]

      val versions = (atom \ "assets").as[JsArray].value.map { asset =>
        (asset \ "version").as[Long]
      }

      Some(MediaAtomSummary(id, title, posterImage, contentChangeDetails))
    }
  }
}

class DynamoBackedAtomListStore(store: PreviewDynamoDataStore) extends AtomListStore {
  override def getAtoms(search: Option[String], limit: Option[Int], shouldUseCreatedDateForSort: Boolean): MediaAtomList = {
    // We must filter the entire list of atoms rather than use Dynamo limit to ensure stable iteration order.
    // Without it, the front page will shuffle around when clicking the Load More button.
    store.listAtoms match {
      case Left(err) =>
        AtomDataStoreError(err.msg)

      case Right(atoms) =>
        def sortField(atom: MediaAtom) =
          if(shouldUseCreatedDateForSort)
            atom.contentChangeDetails.created
          else
            atom.contentChangeDetails.lastModified

        val mediaAtoms = atoms
          .map(MediaAtom.fromThrift)
          .toList
          .sortBy(sortField(_).map(_.date.getMillis))
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

    MediaAtomSummary(atom.id, atom.title, atom.posterImage, atom.contentChangeDetails)
  }
}

object AtomListStore {

  def apply(stage: String, capi: CapiAccess, store: PreviewDynamoDataStore): AtomListStore = stage match {
    case "DEV" => new DynamoBackedAtomListStore(store)
    case _ => new CapiBackedAtomListStore(capi)
  }
}
