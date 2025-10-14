package model

import com.gu.media.logging.Logging
import net.logstash.logback.marker.Markers

import scala.jdk.CollectionConverters._

case class YouTubeMessage(
    atomId: String,
    videoId: String,
    reason: String,
    message: String,
    isError: Boolean = false
) extends Logging {

  def logMessage() =
    if (isError) log.error(createMarkers(), "YouTube Video update")
    else log.info(createMarkers(), "YouTube Video update")

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
