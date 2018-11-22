package com.gu.media.util

import net.logstash.logback.marker.Markers
import play.api.Logger

import scala.collection.JavaConverters._

object MAMLogger {
  private val logger = Logger.logger
  private def atomIdMarker(atomId: String) = Map("atomId" -> atomId)
  private def videoIdMarker(videoId: String) = Map("videoId" -> videoId)

  def info(message: String, markers: Map[String, Any]): Unit = logger.info(Markers.appendEntries(markers.asJava), message)
  def error(message: String, markers: Map[String, Any]): Unit = logger.error(Markers.appendEntries(markers.asJava), message)
  def error(message: String, markers: Map[String, Any], throwable: Throwable): Unit = logger.error(Markers.appendEntries(markers.asJava), message, throwable)

  def info(message: String, atomId: String, videoId: String): Unit = info(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
  def error(message: String, atomId: String, videoId: String): Unit = error(message, atomIdMarker(atomId) ++ videoIdMarker(videoId))
  def error(message: String, atomId: String, videoId: String, throwable: Throwable): Unit = error(message, atomIdMarker(atomId) ++ videoIdMarker(videoId), throwable)
}
