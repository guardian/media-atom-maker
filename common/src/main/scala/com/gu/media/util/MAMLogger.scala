package com.gu.media.util

import com.gu.media.logging.Logging
import net.logstash.logback.marker.Markers

import scala.jdk.CollectionConverters._

object MAMLogger extends Logging {
  private def atomIdMarker(atomId: String) = Map("atomId" -> atomId)
  private def videoIdMarker(videoId: String) = Map("videoId" -> videoId)

  def info(message: String, markers: Map[String, Any]): Unit =
    log.info(Markers.appendEntries(markers.asJava), message)
  def error(message: String, markers: Map[String, Any]): Unit =
    log.error(Markers.appendEntries(markers.asJava), message)
  def error(
      message: String,
      markers: Map[String, Any],
      throwable: Throwable
  ): Unit = log.error(Markers.appendEntries(markers.asJava), message, throwable)

  def info(message: String, atomId: String, videoId: String): Unit =
    info(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
  def error(message: String, atomId: String, videoId: String): Unit =
    error(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
  def error(
      message: String,
      atomId: String,
      videoId: String,
      throwable: Throwable
  ): Unit =
    error(message, atomIdMarker(atomId) ++ videoIdMarker(videoId), throwable)
}
