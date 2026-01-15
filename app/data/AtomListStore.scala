package data

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.media.CapiAccess
import com.gu.media.model.{ContentChangeDetails, Image, MediaAtom}
import com.gu.media.util.TestFilters
import model.commands.CommandExceptions.AtomDataStoreError
import model.{MediaAtomList, MediaAtomSummary}
import play.api.Logging
import play.api.libs.json.{JsArray, JsValue}

import scala.annotation.tailrec

trait AtomListStore {
  def getAtoms(
      search: Option[String],
      limit: Option[Int],
      shouldUseCreatedDateForSort: Boolean,
      mediaPlatform: Option[String],
      orderByOldest: Boolean
  ): MediaAtomList
}

case class CapiPage(
    total: Int,
    pageNumber: Int,
    pages: Int
)

case class CapiAtoms(lastPage: CapiPage, atoms: List[MediaAtomSummary])

object CapiAtoms {
  def empty: CapiAtoms = CapiAtoms(CapiPage(0, 0, 0), Nil)
}

class CapiBackedAtomListStore(capi: CapiAccess)
    extends AtomListStore
    with Logging {

  // CAPI max page size is 200
  val CapiMaxPageSize = 200

  override def getAtoms(
      search: Option[String],
      limit: Option[Int],
      shouldUseCreatedDateForSort: Boolean,
      mediaPlatform: Option[String],
      orderByOldest: Boolean
  ): MediaAtomList = {
    val cappedLimit: Option[Int] = limit.map(Math.min(CapiMaxPageSize, _))

    val dateSorter = shouldUseCreatedDateForSort match {
      case true  => Map("order-date" -> "first-publication")
      case false => Map.empty
    }

    val mediaPlatformFilter = mediaPlatform match {
      case Some(mPlatform) => Map("media-platform" -> mPlatform)
      case _               => Map.empty
    }

    val orderBy = orderByOldest match {
      case true  => Map("order-by" -> "oldest")
      case false => Map("order-by" -> "newest")
    }

    val base: Map[String, String] = Map("types" -> "media") ++
      dateSorter ++
      mediaPlatformFilter ++
      orderBy

    val baseWithSearch = search match {
      case Some(q) =>
        base ++ Map(
          "q" -> q,
          "searchFields" -> "title"
        )
      case None => base
    }

    val baseWithSearchAndLimit = cappedLimit match {
      case Some(pageSize) =>
        baseWithSearch ++ Map(
          "page-size" -> pageSize.toString
        )
      case None => baseWithSearch
    }

    val response =
      getCapiPages(baseWithSearchAndLimit, limit, CapiAtoms.empty)

    val total = response.lastPage.total
    val atoms = limit match {
      case Some(limit) => response.atoms.take(limit)
      case None        => response.atoms
    }

    MediaAtomList(total, atoms)
  }

  @tailrec
  final def getCapiPages(
      query: Map[String, String],
      limit: Option[Int],
      acc: CapiAtoms
  ): CapiAtoms = {
    limit match {
      case None =>
        // get default first page
        getCapiAtoms(query)
      case Some(limit) =>
        // get numbered page of atoms
        val nextPage = acc.lastPage.pageNumber + 1
        val pageResult = getCapiAtoms(query ++ Map("page" -> nextPage.toString))
        // accumulate result
        val result = pageResult.copy(atoms = acc.atoms ++ pageResult.atoms)
        // finished or continue?
        val isLastPage = result.lastPage.pageNumber == result.lastPage.pages
        val isLimitReached = result.atoms.length >= limit
        if (isLastPage || isLimitReached) {
          logger.info(
            s"getCapiPages lastPage: ${result.lastPage}, atoms: ${result.atoms.length}, isLastPage: $isLastPage, isLimitReached: $isLimitReached"
          )
          result
        } else {
          getCapiPages(query, Some(limit), result)
        }
    }
  }

  private[data] def getCapiAtoms(
      query: Map[String, String]
  ): CapiAtoms = {
    val response = capi.capiQuery("atoms", query)
    val total = (response \ "response" \ "total").as[Int]
    val currentPage = (response \ "response" \ "currentPage").as[Int]
    val pages = (response \ "response" \ "pages").as[Int]
    val results = (response \ "response" \ "results").as[JsArray]
    logger.info(
      s"getCapiAtoms total: $total, pages: $pages, results: ${results.value.length}, query: $query"
    )
    CapiAtoms(
      CapiPage(total, currentPage, pages),
      results.value.flatMap(fromJson).toList
    )
  }

  private def fromJson(wrapper: JsValue): Option[MediaAtomSummary] = {
    val id = (wrapper \ "id").as[String]
    val atom = wrapper \ "data" \ "media"

    val category = (atom \ "category").as[String]

    val title = (atom \ "title").as[String]

    if (title.startsWith(TestFilters.testAtomBaseName))
      None /* This filters out test atoms created by the Integration Tests, as we don't want them to be user facing */
    else {
      val posterImage = (atom \ "posterImage").asOpt[Image]

      val activeVersion = (atom \ "activeVersion").asOpt[Long]

      val contentChangeDetails =
        (wrapper \ "contentChangeDetails").as[ContentChangeDetails]

      val expiryDate =
        (wrapper \ "contentChangeDetails" \ "expiry" \ "date").asOpt[Long]

      val versions = (atom \ "assets").as[JsArray].value.map { asset =>
        (asset \ "version").as[Long]
      }

      val mediaPlatforms = (atom \ "assets")
        .as[JsArray]
        .value
        .flatMap { asset =>
          (asset \ "platform").asOpt[String].map(_.toLowerCase)
        }
        .toList
        .distinct

      val currentAsset = (atom \ "assets").as[JsArray].value.find { asset =>
        val assetVersion = (asset \ "version").as[Long]
        activeVersion.contains(assetVersion)
      }

      val currentMediaPlatform = currentAsset.flatMap { asset =>
        (asset \ "platform").asOpt[String].map(_.toLowerCase)
      }

      // sort media platforms so the current one is first
      val sortedMediaPlatforms = currentMediaPlatform match {
        case Some(current) => current :: mediaPlatforms.filter(_ != current)
        case None          => mediaPlatforms
      }

      Some(
        MediaAtomSummary(
          id,
          title,
          posterImage,
          contentChangeDetails,
          sortedMediaPlatforms,
          currentMediaPlatform
        )
      )
    }
  }
}

class DynamoBackedAtomListStore(store: PreviewDynamoDataStore)
    extends AtomListStore {
  override def getAtoms(
      search: Option[String],
      limit: Option[Int],
      shouldUseCreatedDateForSort: Boolean,
      mediaPlatform: Option[String],
      orderByOldest: Boolean
  ): MediaAtomList = {
    // We must filter the entire list of atoms rather than use Dynamo limit to ensure stable iteration order.
    // Without it, the front page will shuffle around when clicking the Load More button.
    store.listAtoms match {
      case Left(err) =>
        AtomDataStoreError(err.msg)

      case Right(atoms) =>
        def sortField(atom: MediaAtom) =
          if (shouldUseCreatedDateForSort)
            atom.contentChangeDetails.created
          else
            atom.contentChangeDetails.lastModified

        val mediaAtoms = atoms
          .map(MediaAtom.fromThrift)
          .toList
          .sortBy(sortField(_).map(_.date.getMillis))

        val mediaAtomsSorted = orderByOldest match {
          case true  => mediaAtoms
          case false => mediaAtoms.reverse
        }

        val searchTermFilter = search match {
          case Some(str) => Some((atom: MediaAtom) => atom.title.contains(str))
          case None      => None
        }

        val mediaPlatformFilter = mediaPlatform match {
          case Some(mPlatform) =>
            Some((atom: MediaAtom) =>
              atom.assets.exists(
                _.platform.name.toLowerCase == mPlatform.toLowerCase
              )
            )
          case _ => None
        }

        val filters = List(searchTermFilter, mediaPlatformFilter).flatten

        val filteredAtoms =
          filters.foldLeft(mediaAtomsSorted)((atoms, f) => atoms.filter(f))

        val limitedAtoms = limit match {
          case Some(l) => filteredAtoms.take(l)
          case None    => filteredAtoms
        }

        MediaAtomList(filteredAtoms.size, limitedAtoms.map(fromAtom))
    }
  }

  private def fromAtom(atom: MediaAtom): MediaAtomSummary = {
    val versions = atom.assets.map(_.version).toSet
    val currentAsset = atom.assets.find(asset =>
      asset.version == atom.activeVersion.getOrElse(versions.max)
    )
    val mediaPlatforms = atom.assets.map(_.platform.name.toLowerCase).distinct
    val currentMediaPlatform =
      currentAsset.map(_.platform.name).map(_.toLowerCase)

    // sort media platforms so the current one is first
    val sortedMediaPlatforms = currentMediaPlatform match {
      case Some(current) => current :: mediaPlatforms.filter(_ != current)
      case None          => mediaPlatforms
    }

    MediaAtomSummary(
      atom.id,
      atom.title,
      atom.posterImage,
      atom.contentChangeDetails,
      sortedMediaPlatforms,
      currentMediaPlatform
    )
  }
}

object AtomListStore {

  def apply(
      stage: String,
      capi: CapiAccess,
      store: PreviewDynamoDataStore
  ): AtomListStore = stage match {
    case "DEV" => new DynamoBackedAtomListStore(store)
    case _     => new CapiBackedAtomListStore(capi)
  }
}

case class Pagination(pageSize: Int, pageCount: Int)

object Pagination {
  def option(maxPageSize: Int, limit: Option[Int]): Option[Pagination] = {
    limit.map { limit =>
      val pageSize = Math.min(maxPageSize, limit)
      val pageCount = Math.ceil(1.0 * limit / maxPageSize).toInt
      Pagination(pageSize, pageCount)
    }
  }
}
