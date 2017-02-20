package com.gu.media.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Region
import com.gu.logback.appender.kinesis.KinesisAppender
import com.typesafe.config.Config
import org.slf4j.{LoggerFactory, Logger => SLFLogger}

trait KinesisLogging {
  private val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def config: Config
  def region: Region

  def getStack: Option[String]
  def getApp: Option[String]
  def getStage: Option[String]

  def startKinesisLogging(credentials: AWSCredentialsProvider): Unit = {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")
    for {
      stack <- getStack
      app <- getApp
      stage <- getStage

      stream <- setting("aws.kinesis.logging", config)
    } yield {
      rootLogger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")

      val context = rootLogger.getLoggerContext
      val appender = new KinesisAppender[ILoggingEvent]()

      appender.setBufferSize(1000)
      appender.setRegion(region.getName)
      appender.setStreamName(stream)
      appender.setContext(context)
      appender.setLayout(Logging.layout(context, stack, app, stage))
      appender.setCredentialsProvider(credentials)
      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }

  private def setting(name: String, config: Config): Option[String] = {
    if(config.hasPath(name)) {
      Some(config.getString(name))
    } else {
      None
    }
  }
}
