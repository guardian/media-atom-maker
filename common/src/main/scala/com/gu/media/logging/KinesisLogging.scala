package com.gu.media.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.logback.appender.kinesis.KinesisAppender
import com.gu.media.aws.AwsAccess
import org.slf4j.{LoggerFactory, Logger => SLFLogger}

trait KinesisLogging { this: AwsAccess =>
  private val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def startKinesisLogging(credentials: AWSCredentialsProvider): Unit = {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")
    for {
      _stack <- stack
      _app <- app
      stream <- getString("aws.kinesis.logging")
    } yield {
      rootLogger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")

      val context = rootLogger.getLoggerContext
      val appender = new KinesisAppender[ILoggingEvent]()

      appender.setBufferSize(1000)
      appender.setRegion(region.getName)
      appender.setStreamName(stream)
      appender.setContext(context)
      appender.setLayout(Logging.layout(context, _stack, _app, stage))
      appender.setCredentialsProvider(credentials)
      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }
}
