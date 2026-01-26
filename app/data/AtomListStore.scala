package data

import com.gu.atom.data.PreviewDynamoDataStoreV2
import com.gu.media.CapiAccess
import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.model.VideoPlayerFormat.Loop
import com.gu.media.model.{
  ContentChangeDetails,
  Image,
  MediaAtom,
  Platform,
  VideoPlayerFormat
}
import com.gu.media.util.TestFilters
import model.commands.CommandExceptions.AtomDataStoreError
import model.{MediaAtomList, MediaAtomSummary}
import play.api.Logging
import play.api.libs.json.{JsArray, JsValue}

trait AtomListStore {
  def getAtoms(
      search: Option[String],
      limit: Option[Int],
      shouldUseCreatedDateForSort: Boolean,
      platformFilter: Option[String],
      orderByOldest: Boolean
  ): MediaAtomList
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
      platformFilter: Option[String],
      orderByOldest: Boolean
  ): MediaAtomList = {
    val pagination = Pagination.option(CapiMaxPageSize, limit)

    val dateSorter = shouldUseCreatedDateForSort match {
      case true  => Map("order-date" -> "first-publication")
      case false => Map.empty
    }

    val mediaPlatformFilter = platformFilter match {
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

    val baseWithSearchAndLimit = pagination match {
      case Some(Pagination(pageSize, _)) =>
        baseWithSearch ++ Map(
          "page-size" -> pageSize.toString
        )
      case None => baseWithSearch
    }

    val nPages = pagination.map(_.pageCount).getOrElse(1)

    val (total, _, atoms) =
      (1 to nPages).foldLeft(0, nPages, List.empty[MediaAtomSummary]) {
        case ((prevTotal, prevMaxPage, prevAtoms), page) =>
          val pageNumber = pagination match {
            case Some(_) => Map("page" -> page.toString)
            case None    => Map.empty
          }
          // make sure we don't request beyond the last page
          val (total, maxPage, atoms) = if (page <= prevMaxPage) {
            getCapiAtoms(baseWithSearchAndLimit ++ pageNumber)
          } else {
            (prevTotal, prevMaxPage, Nil)
          }
          (
            total,
            maxPage,
            prevAtoms ++ atoms
          )
      }

    val limitedAtoms = limit match {
      case Some(limit) => atoms.take(limit)
      case None        => atoms
    }

    logger.info(s"total $total, atoms: ${limitedAtoms.size}")
    MediaAtomList(total, limitedAtoms)
  }

  private[data] def getCapiAtoms(
      query: Map[String, String]
  ): (Int, Int, List[MediaAtomSummary]) = {
    val response = capi.capiQuery("atoms", query)
    val total = (response \ "response" \ "total").as[Int]
    val maxPage = (response \ "response" \ "pages").as[Int]
    val results = (response \ "response" \ "results").as[JsArray]
    (
      total,
      maxPage,
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

      val atomPlatform =
        (atom \ "platform").asOpt[Platform]

      val activeAsset = (atom \ "assets").as[JsArray].value.find { asset =>
        val assetVersion = (asset \ "version").as[Long]
        activeVersion.contains(assetVersion)
      }

      val activeAssetPlatform = activeAsset.map { asset =>
        (asset \ "platform").as[Platform]
      }

      val firstAssetPlatform =
        (atom \ "assets").as[JsArray].value.headOption.map { asset =>
          (asset \ "platform").as[Platform]
        }

      val platform = Platform.getPlatform(
        atomPlatform,
        activeAssetPlatform,
        firstAssetPlatform
      )

      val videoPlayerFormat =
        (atom \ "metadata" \ "selfHost" \ "videoPlayerFormat")
          .asOpt[VideoPlayerFormat]
          .orElse(if (platform == Url) Some(Loop) else None)

      Some(
        MediaAtomSummary(
          id,
          title,
          posterImage,
          contentChangeDetails,
          platform,
          videoPlayerFormat
        )
      )
    }
  }
}

object AtomListStore {

  def apply(
      capi: CapiAccess
  ): AtomListStore = new CapiBackedAtomListStore(capi)
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
