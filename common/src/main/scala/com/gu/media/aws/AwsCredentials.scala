package com.gu.media.aws

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.{AWSSecurityTokenServiceClient}
import com.gu.media.Settings

case class AwsCredentials(instance: AWSCredentialsProvider, crossAccount: AWSCredentialsProvider)

object AwsCredentials {
  def dev(settings: Settings): AwsCredentials = {
    val profile = settings.getMandatoryString("aws.profile")
    val instance = new ProfileCredentialsProvider(profile)

    // To enable publishing to CAPI code from DEV, update the kinesis streams in config and uncomment below:
    //   val crossAccount = new ProfileCredentialsProvider("composer")
    val crossAccount = instance

    AwsCredentials(instance, crossAccount)
  }

  def app(settings: Settings): AwsCredentials = {
    val instance = InstanceProfileCredentialsProvider.getInstance()
    val crossAccount = assumeCrossAccountRole(instance, settings)

    AwsCredentials(instance, crossAccount)
  }

  def lambda(): AwsCredentials = {
    val instance = new EnvironmentVariableCredentialsProvider()
    AwsCredentials(instance, crossAccount = instance)
  }

  private def assumeCrossAccountRole(instance: AWSCredentialsProvider, settings: Settings): AWSCredentialsProvider = {
    val securityTokens = new AWSSecurityTokenServiceClient(instance)

    val crossAccountRole = settings.getMandatoryString("aws.kinesis.stsRoleToAssume",
      "Role to assume to access logging and CAPI streams (in format arn:aws:iam::<account>:role/<role_name>)")

    new STSAssumeRoleSessionCredentialsProvider.Builder(crossAccountRole, "media-atom-maker")
      .withStsClient(securityTokens).build()
  }
}
