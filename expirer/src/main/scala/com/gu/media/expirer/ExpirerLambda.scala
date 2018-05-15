package com.gu.media.expirer

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.CapiAccess
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTubeAccess, YouTubeVideos, YouTubeClaims}
import play.api.libs.json.{JsArray, JsValue}

import scala.annotation.tailrec
import scala.util.control.NonFatal

class ExpirerLambda extends RequestHandler[Unit, Unit]
  with LambdaBase
  with Logging
  with CapiAccess
  with YouTubeAccess
  with YouTubeVideos
  with YouTubeClaims {


  override def handleRequest(input: Unit, context: Context): Unit = {
    val now = Instant.now()
    val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
    val atomsWithAssets: Seq[AssetDetails] = getVideosFromExpiredAtoms(1, 100, oneDayAgo, now, Seq.empty)
      .map(atomWithAssets => {
        val assetIds = atomWithAssets.assetIds.filter(isManagedVideo)
        AssetDetails(atomWithAssets.atomId, assetIds)
    })

    atomsWithAssets.foreach { atomWithAssets =>
      atomWithAssets.assetIds.foreach { assetId =>
        try {
          setStatus(assetId, PrivacyStatus.Private)
          createOrUpdateClaim(atomWithAssets.atomId, assetId, blockAds = true)
        } catch {
          case NonFatal(err) =>
            log.error(s"Error when expiring video $assetId in atom ${atomWithAssets.atomId}", err)
        }
      }
    }
  }

  @tailrec
  private def getVideosFromExpiredAtoms(page: Int, pageSize: Int, fromDate: Instant, toDate: Instant, before: Seq[AssetDetails]): Seq[AssetDetails] = {
    val qs: Map[String, String] = Map(
      "types" -> "media",
      "page-size" -> pageSize.toString,
      "page" -> page.toString,
      "from-date" -> fromDate.toString,
      "to-date" -> toDate.toString,
      "use-date" -> "expiry"
    )

    val response = (capiQuery("atoms", qs, queryLive = true) \ "response").get
    val currentPage = (response \ "currentPage").as[Int]
    val pages = (response \ "pages").as[Int]

    val results = (response \ "results").as[JsArray].value
    val after: Seq[AssetDetails] = before ++ results.map(getExpiredVideos)

    if(currentPage < pages)
      getVideosFromExpiredAtoms(page + 1, pageSize, fromDate, toDate, after)
    else
      after
  }

  private def getExpiredVideos(atom: JsValue): AssetDetails = {
      val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]
      val videos = assets.value.filter { asset => (asset \ "platform").as[String] == "youtube" }
      val ids = videos.map { asset => (asset \ "id").as[String] }

      val atomId = (atom \ "id").as[String]
      AssetDetails(atomId, ids.toSet)
  }

  case class AssetDetails(atomId: String, assetIds: Set[String])

}
