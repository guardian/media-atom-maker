package com.gu.media.aws

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, InstanceProfileCredentialsProvider}
import com.amazonaws.regions.{Region, Regions}
import com.gu.media.Settings

trait AwsAccess extends Settings {
  val region: Region = getRegion
  val credsProvider: AWSCredentialsProvider = getCredentialProvider

  val stack: Option[String] = None
  val app: Option[String] = None
  val stage: String = getString("stage").getOrElse("DEV")

  private def getRegion = {
    getString("aws.region") match {
      case Some(name) => Region.getRegion(Regions.fromName(name))
      case None => Region.getRegion(Regions.EU_WEST_1)
    }
  }

  private def getCredentialProvider = {
    val instanceProvider = InstanceProfileCredentialsProvider.getInstance()

    getString("aws.profile") match {
      case Some(profile) => new AWSCredentialsProviderChain(new ProfileCredentialsProvider(profile), instanceProvider)
      case None => instanceProvider
    }
  }
}
