package com.gu.media.model;

import com.gu.media.logging.Logging
import net.logstash.logback.marker.Markers

import scala.jdk.CollectionConverters._

case class AuditMessage(
    atomId: String,
    auditType: String,
    user: String,
    description: Option[String] = None
) extends Logging {

  def logMessage(): Unit = {
    log.info(createMarkers(), "Media Atom Audit")
  }

  private def createMarkers() =
    Markers.appendEntries(
      (
        Map(
          "atomId" -> atomId,
          "auditType" -> auditType,
          "user" -> user
        )
          ++ description.map("description" -> _)
      ).asJava
    )

}
