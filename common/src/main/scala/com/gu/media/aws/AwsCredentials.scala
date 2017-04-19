package com.gu.media.aws

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.gu.media.Settings

case class AwsCredentials(instance: AWSCredentialsProvider, crossAccount: AWSCredentialsProvider,
                          upload: AWSCredentialsProvider)

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

    AwsCredentials(instance, crossAccount, upload = instance)
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

  private def assumeCrossAccountRole(instance: AWSCredentialsProvider, settings: Settings): AWSCredentialsProvider = {
    val securityTokens = AWSSecurityTokenServiceClientBuilder
      .standard()
      .withCredentials(instance)
      .build()

    val crossAccountRole = settings.getMandatoryString("aws.kinesis.stsRoleToAssume",
      "Role to assume to access logging and CAPI streams (in format arn:aws:iam::<account>:role/<role_name>)")

    new STSAssumeRoleSessionCredentialsProvider.Builder(crossAccountRole, "media-atom-maker")
      .withStsClient(securityTokens).build()
  }
}
