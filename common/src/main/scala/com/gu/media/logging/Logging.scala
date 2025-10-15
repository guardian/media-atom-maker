package com.gu.media.logging

import ch.qos.logback.classic.LoggerContext
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
}

object Logging {
  def layout(
      context: LoggerContext,
      stack: String,
      app: String,
      stage: String
  ): LogstashLayout = {
    val layout = new LogstashLayout()
    layout.setContext(context)
    layout.setCustomFields(
      s"""{"stack":"$stack","app":"$app","stage":"$stage"}"""
    )
    layout.start()

    layout
  }
}
