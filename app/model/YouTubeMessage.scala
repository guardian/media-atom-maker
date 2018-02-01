package model

import net.logstash.logback.marker.Markers
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.JavaConverters._

case class YouTubeMessage(atomId: String, videoId: String, reason: String, message: String, isError: Boolean = false) {

  def logMessage() =
    if (isError) Logger.logger.error(createMarkers(), "YouTube Video update")
    else Logger.logger.info(createMarkers(), "YouTube Video update")



  private def createMarkers() =
    Markers.appendEntries(
      Map(
        "atomId" -> atomId,
        "videoId" -> videoId,
        "reason" -> reason,
        "message" -> message
      ).asJava
    )

}

