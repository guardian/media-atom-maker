package com.gu.media.aws

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.gu.media.Settings
import com.typesafe.config.Config

trait CrossAccountAccess { this: Settings with AwsAccess =>
  def getCrossAccountCredentials(sessionId: String): AWSCredentialsProvider = {
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("composer"),
      new STSAssumeRoleSessionCredentialsProvider(credsProvider, getStsRole(config), sessionId)
    )
  }

  private def getStsRole(config: Config): String = {
    if(config.hasPath("aws.kinesis.stsRoleToAssume")) {
      config.getString("aws.kinesis.stsRoleToAssume")
    } else {
      ""
    }
  }
}
