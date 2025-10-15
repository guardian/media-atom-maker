package com.gu.media.scheduler

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.gu.media.CapiAccess
import com.gu.media.lambda.LambdaBase
import com.gu.media.logging.Logging
import com.gu.media.model.MediaAtom
import com.gu.media.util.HMACClient
import play.api.libs.json.{JsArray, JsValue}

import scala.annotation.tailrec
import scala.util.control.NonFatal

class SchedulerLambda
    extends RequestHandler[Unit, Unit]
    with LambdaBase
    with Logging
    with CapiAccess {

  private val hmacClient = new HMACClient("media-atom-scheduler-lambda", this)

  override def handleRequest(input: Unit, context: Context): Unit = {
    val now = Instant.now()
    val oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS)
    val atomIds = getScheduledAtoms(1, 100, oneDayAgo, now, Set.empty)

    atomIds.foreach { atomId =>
      try {
        val publishedAtom =
          hmacClient.put(buildUri(s"api/atom/$atomId/publish")).as[MediaAtom]
        log.info(
          s"Published scheduled atom. atom=${publishedAtom.id} scheduledLaunch=${publishedAtom.contentChangeDetails.scheduledLaunch.map(_.date)}"
        )
      } catch {
        case NonFatal(err) =>
          log.error(s"Unable to launch atom. atom=$atomId", err)
      }
    }
  }

  @tailrec
  private def getScheduledAtoms(
      page: Int,
      pageSize: Int,
      fromDate: Instant,
      toDate: Instant,
      accumulator: Set[String]
  ): Set[String] = {
    val qs: Map[String, String] = Map(
      "types" -> "media",
      "page-size" -> pageSize.toString,
      "page" -> page.toString,
      "from-date" -> fromDate.toString,
      "to-date" -> toDate.toString,
      "use-date" -> "scheduled-launch"
    )

    val response = (capiQuery("atoms", qs) \ "response").get
    val currentPage = (response \ "currentPage").as[Int]
    val pages = (response \ "pages").as[Int]

    val results: Seq[JsValue] = (response \ "results").as[JsArray].value.toSeq

    val after = accumulator ++ results.map(js => (js \ "id").as[String])

    if (currentPage < pages)
      getScheduledAtoms(page + 1, pageSize, fromDate, toDate, after)
    else
      after
  }
}
