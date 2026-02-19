package com.gu.media.aws

import com.gu.media.Settings

case class AwsCredentials(
    instance: CredentialsForAws,
    crossAccount: CredentialsForAws,
    upload: CredentialsForAws
)

object AwsCredentials {
  def dev(settings: Settings): AwsCredentials = {
    val profile = settings.getMandatoryString("aws.profile")
    val instance = CredentialsForAws.profile(profile)

    // To enable publishing to CAPI code from DEV, update the kinesis streams in config and uncomment below:
    val crossAccount = CredentialsForAws.profile("composer")
    // val crossAccount = instance

    val upload = devUpload(settings)

    AwsCredentials(instance, crossAccount, upload)
  }

  def app(settings: Settings): AwsCredentials = {
    val instance = CredentialsForAws.instance()

    val crossAccount = assumeCrossAccountRole(instance, settings)

    AwsCredentials(instance, crossAccount, upload = instance)
  }

  def lambda(): AwsCredentials = {
    val instance = CredentialsForAws.environmentVariables()
    AwsCredentials(instance, crossAccount = instance, upload = instance)
  }

  private def devUpload(settings: Settings): CredentialsForAws = {
    // Only required in dev (because federated credentials such as those from Janus cannot do STS requests).
    // Instance profile credentials are sufficient when deployed.
    val accessKey = settings.getMandatoryString(
      "aws.upload.accessKey",
      "This is the AwsId output of the dev cloudformation"
    )
    val secretKey = settings.getMandatoryString(
      "aws.upload.secretKey",
      "This is the AwsSecret output of the dev cloudformation"
    )

    CredentialsForAws.static(accessKey, secretKey)
  }

  private def assumeCrossAccountRole(
      instance: CredentialsForAws,
      settings: Settings
  ): CredentialsForAws = {
    val crossAccountRoleArn = settings.getMandatoryString(
      "aws.kinesis.stsCapiRoleToAssume",
      "Role to assume to access CAPI streams (in format arn:aws:iam::<account>:role/<role_name>)"
    )

    instance.assumeAccountRole(
      crossAccountRoleArn,
      "capi",
      AwsAccess.regionFrom(settings).id()
    )
  }
}
