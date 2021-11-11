package com.gu.media.aws

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.gu.media.Settings

trait AwsAccess { this: Settings =>
  def regionName: Option[String]
  def readTag(tag: String): Option[String]

  val credentials: AwsCredentials
  // To avoid renaming references everywhere
  def credsProvider: AWSCredentialsProvider = credentials.instance

  final def defaultRegion: Region = Region.getRegion(Regions.EU_WEST_1)
  final def region: Region = regionName
    .map { name => Region.getRegion(Regions.fromName(name)) }
    .getOrElse(defaultRegion)

  // These are injected as environment variables when running in a Lambda (unfortunately they cannot be tagged)
  final val stage = sys.env.getOrElse("STAGE", readTag("Stage").getOrElse("DEV"))
  final val isDev: Boolean = stage == "DEV"

  final val stack: Option[String] = if (isDev) Some("media-atom-maker") else readTag("Stack")
  final val app: String = if (isDev) "media-atom-maker" else readTag("App").getOrElse("media-atom-maker")
}
