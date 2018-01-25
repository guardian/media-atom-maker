package model

import net.logstash.logback.marker.Markers
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.JavaConverters._

case class YouTubeMessage(atomId: String, videoId: String, reason: String, message: Either[String, String]) {

  def logMessage = message match {
    case Right(message) => Logger.logger.info(createMarkers(message), "YouTube update")
    case Left(message) => Logger.logger.error(createMarkers(message), "YouTube update")
  }

  private def createMarkers(message: String) =
    Markers.appendEntries(
      Map(
        "atomId" -> atomId,
        "videoId" -> videoId,
        "reason" -> reason,
        "message" -> message
      ).asJava
    )

}
