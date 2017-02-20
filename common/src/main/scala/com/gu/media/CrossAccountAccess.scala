package com.gu.media

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.typesafe.config.Config

abstract class CrossAccountAccess(config: Config) {
  def getCrossAccountCredentials(credProvider: AWSCredentialsProvider, sessionId: String): AWSCredentialsProvider = {
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("composer"),
      new STSAssumeRoleSessionCredentialsProvider.Builder(getStsRole(config), sessionId).build()
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
