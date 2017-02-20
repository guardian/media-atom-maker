package com.gu.media.lambda

import java.io.InputStreamReader

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, EnvironmentVariableCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.media.logging.LambdaElkLoggingFormat
import com.typesafe.config.{Config, ConfigFactory}

trait LambdaBase {
  private val regionName = envSetting("REGION")
  private val configBucket = envSetting("CONFIG_BUCKET")
  private val configKey = envSetting("CONFIG_KEY")
  private val localConfig = ConfigFactory.load()

  val region: Region = Region.getRegion(Regions.fromName(regionName))
  val credsProvider: AWSCredentialsProvider = buildCredsProvider(localConfig)

  // Creating these outside the handleRequest call means they can be re-used across invocations
  // http://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
  val (s3, config) = buildS3Client(localConfig)

  private def buildS3Client(localConfig: Config): (AmazonS3Client, Config) = {
    val s3: AmazonS3Client = region.createClient(classOf[AmazonS3Client], credsProvider, null)

    val obj = s3.getObject(configBucket, configKey)
    val rawConfig = obj.getObjectContent
    val fromS3 = ConfigFactory.parseReader(new InputStreamReader(rawConfig))

    val config = fromS3.withFallback(localConfig)

    (s3, config)
  }

  private def buildCredsProvider(config: Config): AWSCredentialsProvider = {
    if(config.hasPath("aws.profile")) {
      new ProfileCredentialsProvider(config.getString("aws.profile"))
    } else {
      new EnvironmentVariableCredentialsProvider()
    }
  }

  private def envSetting(name: String): String = sys.env.getOrElse(name, {
    throw new IllegalArgumentException(s"Missing environment variable $name. Check the Lambda configuration")
  })
}
