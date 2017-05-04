package com.gu.media.lambda

import java.io.InputStreamReader
import java.util.Locale

import com.amazonaws.services.s3.AmazonS3Client
import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, AwsCredentials}
import com.typesafe.config.{Config, ConfigFactory}

trait LambdaBase extends Settings with AwsAccess {
  final override def regionName = sys.env.get("REGION")
  final override def readTag(tag: String) = sys.env.get(tag.toUpperCase(Locale.ENGLISH))

  final override val credentials = AwsCredentials.lambda()

  private val remoteConfig = downloadConfig()
  private val mergedConfig = remoteConfig.withFallback(ConfigFactory.load())

  override def config: Config = mergedConfig

  private def downloadConfig(): Config = {
    (sys.env.get("CONFIG_BUCKET"), sys.env.get("CONFIG_KEY")) match {
      case (Some(bucket), Some(key)) =>
        val defaultRegionS3 = defaultRegion.createClient(classOf[AmazonS3Client], credentials.instance, null)

        val obj = defaultRegionS3.getObject(bucket, key)
        val rawConfig = obj.getObjectContent
        ConfigFactory.parseReader(new InputStreamReader(rawConfig))

      case _ =>
        ConfigFactory.empty()
    }
  }

  private def getDevProfile = {
    val localConfig = ConfigFactory.load()

    // we must use the local config, as we may not have downloaded the remote stuff yet
    if(localConfig.hasPath("aws.profile")) {
      Some(localConfig.getString("aws.profile"))
    } else {
      None
    }
  }
}
