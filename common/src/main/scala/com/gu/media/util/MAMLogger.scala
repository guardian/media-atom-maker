package com.gu.media.util

import net.logstash.logback.marker.Markers
import play.api.Logger

import scala.collection.JavaConverters._

object MAMLogger {
  private val logger = Logger.logger
  private def atomIdMarker(atomId: String) = Map("atomId" -> atomId)
  private def videoIdMarker(videoId: String) = Map("videoId" -> videoId)

  def info(message: String, markers: Map[String, Any] = Map()): Unit = logger.info(Markers.appendEntries(markers.asJava), message)
  def error(message: String, markers: Map[String, Any] = Map()): Unit = logger.error(Markers.appendEntries(markers.asJava), message)

  def info(message: String, atomId: String, videoId: String): Unit = info(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
  def error(message: String, atomId: String, videoId: String): Unit = info(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
}
