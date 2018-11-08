package com.gu.media.model;

import net.logstash.logback.marker.Markers
import play.api.Logger

import scala.collection.JavaConverters._

case class AuditMessage(atomId: String, auditType: String, user: String, description: Option[String] = None) {

  def logMessage() {
    Logger.logger.info(createMarkers(), "Media Atom Audit")
  }

  def logMessageToCloudwatch(): Unit = {
    Logger.logger.info(s"Media Atom Audit: atomId=$atomId auditType: $auditType user: $user description: ${description.getOrElse("none")}")
  }

  private def createMarkers() =
    Markers.appendEntries((
      Map(
        "atomId" -> atomId,
        "auditType" -> auditType,
        "user" -> user
      )
        ++ description.map("description" -> _)
      ).asJava
    )

}
