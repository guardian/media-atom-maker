package com.gu.media.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import org.slf4j.{LoggerFactory, Logger => SLFLogger}

trait LambdaElkLoggingFormat {
  private val rootLogger = LoggerFactory
    .getLogger(SLFLogger.ROOT_LOGGER_NAME)
    .asInstanceOf[LogbackLogger]

  (sys.env.get("STACK"), sys.env.get("APP"), sys.env.get("STAGE")) match {
    case (Some(stack), Some(app), Some(stage)) =>
      val newAppender = createAppender(stack, app, stage)

      disableExistingAppender()
      rootLogger.addAppender(newAppender)

    case _ =>
    // leave logging alone
  }

  private def createAppender(stack: String, app: String, stage: String) = {
    val layout = Logging.layout(rootLogger.getLoggerContext, stack, app, stage)

    val encoder = new LayoutWrappingEncoder[ILoggingEvent]
    encoder.setLayout(layout)

    val appender = new ConsoleAppender[ILoggingEvent]
    appender.setEncoder(encoder)
    appender.start()

    appender
  }

  private def disableExistingAppender() = {
    Option(rootLogger.getAppender("console")).foreach(_.stop())
  }
}
