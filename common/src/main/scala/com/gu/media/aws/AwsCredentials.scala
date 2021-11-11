package com.gu.media.aws

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.{AWSSecurityTokenServiceClient}
import com.gu.media.Settings

case class AwsCredentials(instance: AWSCredentialsProvider, crossAccount: AWSCredentialsProvider,
                          upload: AWSCredentialsProvider, logging: Option[AWSCredentialsProvider] = None)

object AwsCredentials {
  def dev(settings: Settings): AwsCredentials = {
    val profile = settings.getMandatoryString("aws.profile")
    val instance = new ProfileCredentialsProvider(profile)

    // To enable publishing to CAPI code from DEV, update the kinesis streams in config and uncomment below:
    //   val crossAccount = new ProfileCredentialsProvider("composer")
    val crossAccount = instance

    val upload = devUpload(settings)

    AwsCredentials(instance, crossAccount, upload)
  }

  def app(settings: Settings): AwsCredentials = {
    val instance = InstanceProfileCredentialsProvider.getInstance()

    val crossAccount = assumeCrossAccountRole(instance, settings)
    val loggingCreds = assumeLoggingRole(instance, settings)

    AwsCredentials(instance, crossAccount, upload = instance, Some(loggingCreds))
  }

  def lambda(): AwsCredentials = {
    val instance = new EnvironmentVariableCredentialsProvider()
    AwsCredentials(instance, crossAccount = instance, upload = instance)
  }

  private def devUpload(settings: Settings): AWSCredentialsProvider = {
    // Only required in dev (because federated credentials such as those from Janus cannot do STS requests).
    // Instance profile credentials are sufficient when deployed.
    val accessKey = settings.getMandatoryString("aws.upload.accessKey", "This is the AwsId output of the dev cloudformation")
    val secretKey = settings.getMandatoryString("aws.upload.secretKey", "This is the AwsSecret output of the dev cloudformation")

    new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))
  }

  private def assumeCrossAccountRole(instance: AWSCredentialsProvider, settings: Settings) = {
    val crossAccountRoleArn = settings.getMandatoryString("aws.kinesis.stsCapiRoleToAssume",
      "Role to assume to access CAPI streams (in format arn:aws:iam::<account>:role/<role_name>)")

    assumeAccountRole(instance, crossAccountRoleArn)
  }

  private def assumeLoggingRole(instance: AWSCredentialsProvider, settings: Settings) = {
    val loggingRoleArn = settings.getMandatoryString("aws.kinesis.stsLoggingRoleToAssume",
      "Role to assume to access logging stream (in format arn:aws:iam::<account>:role/<role_name>)")

    assumeAccountRole(instance, loggingRoleArn)
  }

  private def assumeAccountRole(instance: AWSCredentialsProvider, roleArn: String): AWSCredentialsProvider = {
    val securityTokens = new AWSSecurityTokenServiceClient(instance)

    new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, "media-atom-maker")
      .withStsClient(securityTokens).build()
  }
}
