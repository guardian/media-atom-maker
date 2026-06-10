package com.gu.media.lambda

import java.io.InputStreamReader
import java.util.Locale
import com.gu.media.Settings
import com.gu.media.aws.{AwsAccess, AwsCredentials, HMACSettings}
import com.typesafe.config.{Config, ConfigFactory}
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

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
        val defaultRegionS3 = S3Client
          .builder()
          .credentialsProvider(credentials.instance.awsV2Creds)
          .region(awsV2Region)
          .build()

        val inputStream = defaultRegionS3.getObject(
          GetObjectRequest.builder().bucket(bucket).key(key).build()
        )
        ConfigFactory.parseReader(new InputStreamReader(inputStream))

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
