package util

import javax.inject.{Inject, Singleton}

import ch.qos.logback.classic.{Logger => LogbackLogger}
import com.amazonaws.regions.Region
import com.gu.media.CrossAccountAccess
import com.gu.media.logging.KinesisLogging
import com.typesafe.config.Config
import org.slf4j.{LoggerFactory, Logger => SLFLogger}

trait LogShipping {
  //
}

@Singleton
class LogShippingImpl @Inject() (val awsConfig: AWSConfig) extends CrossAccountAccess(awsConfig.config.underlying)
  with LogShipping with KinesisLogging {

  private val rootLogger = LoggerFactory.getLogger(SLFLogger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]
  private val credsProvider = getCrossAccountCredentials(awsConfig.credProvider, "media-atom-maker-logging")

  override def config: Config = awsConfig.config.underlying
  override def region: Region = awsConfig.region

  override def getStack: Option[String] = awsConfig.readTag("Stack")
  override def getApp: Option[String] = awsConfig.readTag("App")
  override def getStage: Option[String] = awsConfig.readTag("Stage")

  startKinesisLogging(credsProvider)
}
