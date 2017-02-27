package com.gu.media.lambda

import java.io.InputStreamReader

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, EnvironmentVariableCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.gu.media.aws.AwsAccess
import com.typesafe.config.{Config, ConfigFactory}

trait LambdaBase extends AwsAccess {
  private val localConfig = ConfigFactory.load()
  private val defaultRegion = Region.getRegion(Regions.EU_WEST_1)

  override val credsProvider: AWSCredentialsProvider = buildCredsProvider(localConfig)

  // Creating these outside the handleRequest call means they can be re-used across invocations
  // http://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
  override val (region, config) = loadRegionAndConfig()

  override val stack = sys.env.get("STACK")
  override val app = sys.env.get("APP")
  override val stage = sys.env.getOrElse("STAGE", "DEV")

  private def loadRegionAndConfig(): (Region, Config) = {
    (sys.env.get("REGION"), sys.env.get("CONFIG_BUCKET"), sys.env.get("CONFIG_KEY")) match {
      case (Some(regionName), Some(bucket), Some(key)) =>
        val region = Region.getRegion(Regions.fromName(regionName))
        val s3 = region.createClient(classOf[AmazonS3Client], credsProvider, null)
        val config = configFromBucket(s3, bucket, key, localConfig)

        (region, config)

      case _ =>
        val s3 = defaultRegion.createClient(classOf[AmazonS3Client], credsProvider, null)
        (defaultRegion, localConfig)
    }
  }

  private def configFromBucket(s3: AmazonS3Client, bucket: String, key: String, localConfig: Config): Config = {
    val obj = s3.getObject(bucket, key)
    val rawConfig = obj.getObjectContent
    val fromS3 = ConfigFactory.parseReader(new InputStreamReader(rawConfig))

    fromS3.withFallback(localConfig)
  }

  private def buildCredsProvider(config: Config): AWSCredentialsProvider = {
    if(config.hasPath("aws.profile")) {
      new ProfileCredentialsProvider(config.getString("aws.profile"))
    } else {
      new EnvironmentVariableCredentialsProvider()
    }
  }

  def mandatoryEnvString(name: String): String = sys.env.getOrElse(name, {
    throw new IllegalArgumentException(s"Missing environment variable $name. Check the Lambda configuration")
  })
}
