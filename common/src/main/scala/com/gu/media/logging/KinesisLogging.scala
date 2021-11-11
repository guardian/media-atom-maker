package com.gu.media.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.gu.logback.appender.kinesis.KinesisAppender
import com.gu.media.Settings
import com.gu.media.aws.AwsAccess
import org.slf4j.{LoggerFactory, Logger => SLFLogger}

trait KinesisLogging { this: Settings with AwsAccess =>
  private val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def startKinesisLogging(sessionId: String): Unit = {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")

    for {
      _stack <- stack
      loggingCredentials <- credentials.logging
      stream <- getString("aws.kinesis.logging")
    } yield {
      rootLogger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")

      val context = rootLogger.getLoggerContext
      val appender = new KinesisAppender[ILoggingEvent]()

      appender.setBufferSize(1000)
      appender.setRegion(region.getName)
      appender.setStreamName(stream)
      appender.setContext(context)
      appender.setLayout(Logging.layout(context, _stack, app, stage))
      appender.setCredentialsProvider(loggingCredentials)
      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }
}
