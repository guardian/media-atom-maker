package com.gu.media.scheduler

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

class SchedulerLambda extends RequestHandler[Unit, Unit]
  with LambdaBase
  with Logging
  with CapiAccess
  with YouTubeAccess
  with YouTubeVideos {

  def scheduleInParallel = true // disabled in unit tests

  override def handleRequest(input: Unit, context: Context): Unit = {
    val now = Instant.now()
    val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
    val assets = getVideosFromScheduledAtoms(1, 100, oneDayAgo, now, Set.empty).filter(isManagedVideo)

    val toLaunch = if(scheduleInParallel) { assets.par } else { assets }

    toLaunch.foreach { video =>
      try {
        setStatus(video, PrivacyStatus.Public)
      } catch {
        case NonFatal(err) =>
          log.error(s"Unable to launch $video", err)
      }
    }
  }

  @tailrec
  private def getVideosFromScheduledAtoms(page: Int, pageSize: Int, oneDayAgo: Instant, now: Instant, before: Set[String]): Set[String] = {
    val url = s"atoms?types=media&page-size=$pageSize&from-date=$oneDayAgo&to-date=$now&use-date=scheduled-launch"
    val response = (capiQuery(url) \ "response").get
    val currentPage = (response \ "currentPage").as[Int]
    val pages = (response \ "pages").as[Int]

    val results = (response \ "results").as[JsArray].value
    val after = results.foldLeft(before) { (acc, atom) => acc ++ getScheduledVideos(atom) }

    if(currentPage < pages)
      getVideosFromScheduledAtoms(page + 1, pageSize, oneDayAgo, now, after)
    else
      after
  }

  private def getScheduledVideos(atom: JsValue): Set[String] = {
    val scheduledLaunchDate = (atom \ "contentChangeDetails" \ "scheduledLaunch" \ "date").as[Long]

    (atom \ "contentChangeDetails" \ "embargo" \ "date").asOpt[Long] match {
      case Some(embargoDate) if embargoDate < scheduledLaunchDate =>
        val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]
        val videos = assets.value.filter { asset => (asset \ "platform").as[String] == "youtube" }
        val ids = videos.map { asset => (asset \ "id").as[String] }

        ids.toSet
      case None =>
        val assets = (atom \ "data" \ "media" \ "assets").as[JsArray]
        val videos = assets.value.filter { asset => (asset \ "platform").as[String] == "youtube" }
        val ids = videos.map { asset => (asset \ "id").as[String] }

        ids.toSet
      case _ =>
        Set.empty
    }
  }
}
