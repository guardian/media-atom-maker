package com.gu.media.expirer

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.CapiAccess
import com.gu.media.aws.SESSettings
import com.gu.media.lambda.{LambdaBase, LambdaYoutubeCredentials}
import com.gu.media.logging.Logging
import com.gu.media.model.AdSettings
import com.gu.media.ses.Mailer
import com.gu.media.youtube.{YouTubeAccess, YouTubePartnerApi, YouTubeVideos}
import play.api.libs.json.{JsArray, JsValue}

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}
import scala.annotation.tailrec
import scala.util.control.NonFatal

class ExpirerLambda
    extends RequestHandler[Unit, Unit]
    with LambdaBase
    with Logging
    with CapiAccess
    with YouTubeAccess
    with LambdaYoutubeCredentials
    with YouTubeVideos
    with YouTubePartnerApi
    with SESSettings {

  private val mailer = new Mailer(this)

  private val invocationInterval = Duration.ofMinutes(15)

  override def handleRequest(input: Unit, context: Context): Unit = {
    // Atoms should be set to expire at either 15, 30, 45 or 0 minutes past the hour exactly.
    // This lambda is also scheduled to run on the same schedule - but will never run at _exactly_
    // those times, so it'll run either a little early or a little late. We don't want to miss an
    // expiry, so add a minute to "now" to account for any early running, and make sure that eg.
    // a lambda invoked at 15:14:58 does expire an atom scheduled for 15:15:00.
    val now = Instant.now().plusSeconds(60)
    val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
    val atomsWithAssets: Seq[AssetDetails] =
      getVideosFromExpiredAtoms(1, 100, oneDayAgo, now, Seq.empty)
        .map(atomWithAssets => {
          val managedAssetIds = atomWithAssets.assetIds.filter(isManagedVideo)
          atomWithAssets.copy(assetIds = managedAssetIds)
        })

    atomsWithAssets.foreach { atomWithAssets =>
      atomWithAssets.assetIds.foreach { assetId =>
        try {
          log.info(s"Expiring video $assetId in atom ${atomWithAssets.atomId}")
          setStatus(assetId, PrivacyStatus.Private)
          createOrUpdateClaim(atomWithAssets.atomId, assetId, AdSettings.NONE)
        } catch {
          case NonFatal(err) =>
            log.error(
              s"Error when expiring video $assetId in atom ${atomWithAssets.atomId}",
              err
            )
        }
      }

      // The above will idempotently expire all atoms in the past day which is great except
      // when we want to send notifications - we don't want to send an email every 15 mins
      // for the rest of the day! Instead, only select those atoms which expired in the last
      // interval (from the offsetted "now" above)
      val sinceLastRun = now.minus(invocationInterval)

      if (
        atomWithAssets.assetIds.nonEmpty
        && atomWithAssets.atomExpiry.isAfter(sinceLastRun)
        && atomWithAssets.atomExpiry.isBefore(now)
      ) {
        log.info(
          s"Mailing $expiryNotificationsAddress to notify that atom ${atomWithAssets.atomId} was expired"
        )
        mailer.sendAtomExpiredEmail(
          atomId = atomWithAssets.atomId,
          atomTitle = atomWithAssets.atomTitle,
          sendTo = expiryNotificationsAddress
        )
      }
    }
  }

  @tailrec
  private def getVideosFromExpiredAtoms(
      page: Int,
      pageSize: Int,
      fromDate: Instant,
      toDate: Instant,
      before: Seq[AssetDetails]
  ): Seq[AssetDetails] = {
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

    if (currentPage < pages)
      getVideosFromExpiredAtoms(page + 1, pageSize, fromDate, toDate, after)
    else
      after
  }

  private def getExpiredVideos(atom: JsValue): AssetDetails = {
    val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]
    val videos = assets.value.filter { asset =>
      (asset \ "platform").as[String] == "youtube"
    }
    val ids = videos.map { asset => (asset \ "id").as[String] }
    val expiry = (atom \ "data" \ "media" \ "metadata" \ "expiryDate").as[Long]
    val expiryDate = Instant.ofEpochMilli(expiry)

    val atomId = (atom \ "id").as[String]
    val atomTitle = (atom \ "title").as[String]
    AssetDetails(atomId, atomTitle, expiryDate, ids.toSet)
  }

  case class AssetDetails(
      atomId: String,
      atomTitle: String,
      atomExpiry: Instant,
      assetIds: Set[String]
  )

}
