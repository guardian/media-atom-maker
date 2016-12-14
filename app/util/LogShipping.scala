package util

import javax.inject.{Inject, Singleton}

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.gu.logback.appender.kinesis.KinesisAppender
import net.logstash.logback.layout.LogstashLayout
import org.slf4j.{LoggerFactory, Logger => SLFLogger}
import play.api.Logger


trait LogShipping {
  def init(): Unit
}


@Singleton
class LogShippingImpl @Inject() (val awsConfig: AWSConfig) extends LogShipping {
  val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

  def init() {
    rootLogger.info("bootstrapping kinesis appender if configured correctly")
    for (
      stack <- awsConfig.readTag("Stack");
      app <- awsConfig.readTag("App");
      stage <- awsConfig.readTag("Stage");
      stream <- awsConfig.loggingKinesisStreamName
    ) {

      Logger.info(s"bootstrapping kinesis appender with $stack -> $app -> $stage")
      val context = rootLogger.getLoggerContext

      val layout = new LogstashLayout()
      layout.setContext(context)
      layout.setCustomFields(s"""{"stack":"$stack","app":"$app","stage":"$stage"}""")
      layout.start()

      val appender = new KinesisAppender[ILoggingEvent]()
      appender.setBufferSize(1000)
      appender.setRegion(awsConfig.region.getName)
      appender.setStreamName(stream)
      appender.setContext(context)
      appender.setLayout(layout)
      appender.setCredentialsProvider(awsConfig.atomsCredProvider)
      appender.start()

      rootLogger.addAppender(appender)
      rootLogger.info("Configured kinesis appender")
    }
  }

  init()

}
