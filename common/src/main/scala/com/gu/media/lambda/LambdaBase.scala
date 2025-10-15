package com.gu.media.lambda

import java.io.InputStreamReader
import java.util.Locale

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, AwsCredentials, HMACSettings}
import com.typesafe.config.{Config, ConfigFactory}

trait LambdaBase extends Settings with AwsAccess with HMACSettings {
  final override def region = AwsAccess.regionFrom(sys.env.get("REGION"))
  final override def readTag(tag: String) =
    sys.env.get(tag.toUpperCase(Locale.ENGLISH))

  final override val credentials: AwsCredentials = AwsCredentials.lambda()

  private val remoteConfig = downloadConfig()
  private val mergedConfig = remoteConfig.withFallback(ConfigFactory.load())

  override def config: Config = mergedConfig

  private def downloadConfig(): Config = {
    (sys.env.get("CONFIG_BUCKET"), sys.env.get("CONFIG_KEY")) match {
      case (Some(bucket), Some(key)) =>
        val defaultRegionS3 = AmazonS3ClientBuilder
          .standard()
          .withCredentials(credentials.instance.awsV1Creds)
          .withRegion(region.getName)
          .build()

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
    if (localConfig.hasPath("aws.profile")) {
      Some(localConfig.getString("aws.profile"))
    } else {
      None
    }
  }
}
