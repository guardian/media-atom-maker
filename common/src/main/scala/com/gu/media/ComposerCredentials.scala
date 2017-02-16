package com.gu.media

import com.amazonaws.auth.{AWSCredentialsProvider, AWSCredentialsProviderChain, STSAssumeRoleSessionCredentialsProvider}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.typesafe.config.Config

trait ComposerCredentials {
  def getComposerCredentials(credProvider: AWSCredentialsProvider, config: Config): AWSCredentialsProvider = {
    val role = getRole(config)
    val sessionId = "session" + Math.random()

    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("composer"), new STSAssumeRoleSessionCredentialsProvider(credProvider, role, sessionId)
    )
  }

  private def getRole(config: Config): String = {
    if(config.hasPath("aws.kinesis.stsRoleToAssume")) {
      config.getString("aws.kinesis.stsRoleToAssume")
    } else {
      ""
    }
  }
}
