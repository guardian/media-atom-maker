package com.gu.media.aws

import com.amazonaws.auth._
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import software.amazon.awssdk.auth.{credentials => awsv2}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sts.{StsClient, StsClientBuilder}
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest

case class CredentialsForBothSdkVersions(
    awsV1Creds: com.amazonaws.auth.AWSCredentialsProvider,
    awsV2Creds: software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
) {
  def assumeAccountRole(
      roleArn: String,
      sessionNameSuffix: String,
      regionName: String
  ): CredentialsForBothSdkVersions = {
    val roleSessionName = s"media-atom-maker-$sessionNameSuffix"
    CredentialsForBothSdkVersions(
      new STSAssumeRoleSessionCredentialsProvider.Builder(
        roleArn,
        roleSessionName
      )
        .withStsClient(
          AWSSecurityTokenServiceClientBuilder
            .standard()
            .withCredentials(awsV1Creds)
            .withRegion(regionName)
            .build()
        )
        .build(),
      software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider
        .builder()
        .stsClient(
          AwsV2Util.buildSync[StsClient, StsClientBuilder](
            StsClient.builder(),
            awsV2Creds,
            Region.of(regionName)
          )
        )
        .refreshRequest(
          AssumeRoleRequest.builder
            .roleSessionName(roleSessionName)
            .roleArn(roleArn)
            .build
        )
        .build()
    )
  }
}

object CredentialsForBothSdkVersions {
  def profile(name: String): CredentialsForBothSdkVersions =
    CredentialsForBothSdkVersions(
      new ProfileCredentialsProvider(name),
      awsv2.ProfileCredentialsProvider.create(name)
    )

  def instance(): CredentialsForBothSdkVersions = CredentialsForBothSdkVersions(
    InstanceProfileCredentialsProvider.getInstance(),
    awsv2.InstanceProfileCredentialsProvider.create()
  )

  def environmentVariables(): CredentialsForBothSdkVersions =
    CredentialsForBothSdkVersions(
      new EnvironmentVariableCredentialsProvider(),
      awsv2.EnvironmentVariableCredentialsProvider.create()
    )

  def static(
      accessKey: String,
      secretKey: String
  ): CredentialsForBothSdkVersions = CredentialsForBothSdkVersions(
    new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(accessKey, secretKey)
    ),
    awsv2.StaticCredentialsProvider.create(
      awsv2.AwsBasicCredentials.create(accessKey, secretKey)
    )
  )
}
