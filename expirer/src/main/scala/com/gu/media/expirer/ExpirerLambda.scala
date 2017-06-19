package com.gu.media.expirer

import java.time.Instant

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
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

  def expireInParallel = true // disabled in unit tests

  override def handleRequest(input: Unit, context: Context): Unit = {
    val epochMillis = Instant.now().toEpochMilli
    val assets = getVideosFromExpiredAtoms(100, 1, epochMillis, Set.empty)

    val toExpire = if(expireInParallel) { assets.par } else { assets }

    toExpire.foreach { video =>
      try {
        setStatus(video, "Private")
      } catch {
        case NonFatal(err) =>
          log.error(s"Unable to expire $video", err)
      }
    }
  }

  @tailrec
  private def getVideosFromExpiredAtoms(pageSize: Int, page: Int, now: Long, before: Set[String]): Set[String] = {
    val url = s"atoms?types=media&page-size=$pageSize&page=$page"
    val response = (capiQuery(url) \ "response").get

    val currentPage = (response \ "currentPage").as[Int]
    val pages = (response \ "pages").as[Int]

    val results = (response \ "results").as[JsArray].value
    val after = results.foldLeft(before) { (acc, atom) => acc ++ getExpiredVideos(atom, now) }

    if(currentPage < pages)
      getVideosFromExpiredAtoms(pageSize, page + 1, now, after)
    else
      after
  }

  private def getExpiredVideos(atom: JsValue, now: Long): Set[String] = {
    (atom \ "data" \ "media" \ "metadata" \ "expiryDate").asOpt[Long] match {
      case Some(expiryDate) if expiryDate < now =>
        val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]

        val videos = assets.value.filter { asset => (asset \ "platform").as[String] == "youtube" }
        val ids = videos.map { asset => (asset \ "id").as[String] }

        ids.toSet

      case _ =>
        Set.empty
    }
  }
}
