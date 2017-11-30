package com.gu.media.expirer

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.contentatom.thrift.atom.media.PrivacyStatus
import com.gu.media.CapiAccess
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.youtube.{YouTubeAccess, YouTubeVideos}
import play.api.libs.json.{JsArray, JsValue}

import scala.annotation.tailrec
import scala.util.control.NonFatal

class ExpirerLambda extends RequestHandler[Unit, Unit]
  with LambdaBase
  with Logging
  with CapiAccess
  with YouTubeAccess
  with YouTubeVideos {


  override def handleRequest(input: Unit, context: Context): Unit = {
    val now = Instant.now()
    val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
    val assets = getVideosFromExpiredAtoms(1, 100, oneDayAgo, now, Set.empty).filter(isManagedVideo)

    assets.foreach { video =>
      try {
        setStatus(video, PrivacyStatus.Private)
      } catch {
        case NonFatal(err) =>
          log.error(s"Unable to expire $video", err)
      }
    }
  }

  @tailrec
  private def getVideosFromExpiredAtoms(page: Int, pageSize: Int, oneDayAgo: Instant, now: Instant, before: Set[String]): Set[String] = {
    val url = s"atoms?types=media&page-size=$pageSize&page=$page&from-date=$oneDayAgo&to-date=$now&use-date=expiry"
    val response = (capiQuery(url) \ "response").get
    val currentPage = (response \ "currentPage").as[Int]
    val pages = (response \ "pages").as[Int]

    val results = (response \ "results").as[JsArray].value
    val after = results.foldLeft(before) { (acc, atom) => acc ++ getExpiredVideos(atom) }

    if(currentPage < pages)
      getVideosFromExpiredAtoms(page + 1, pageSize, oneDayAgo, now, after)
    else
      after
  }

  private def getExpiredVideos(atom: JsValue): Set[String] = {
      val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]
      val videos = assets.value.filter { asset => (asset \ "platform").as[String] == "youtube" }
      val ids = videos.map { asset => (asset \ "id").as[String] }

      ids.toSet
  }
}
