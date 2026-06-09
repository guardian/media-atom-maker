package com.gu.media.aws

import com.gu.media.Settings
import com.gu.media.telemetry.SecretsManager.secretsManagerClient
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

import scala.util.Try

trait SecretsManagerAccess { this: Settings with AwsAccess =>
  val secretsManagerClient = SecretsManagerClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .build()

  def getSecret(secretId: String) = {
    val secretValueRequest =
      GetSecretValueRequest.builder.secretId(secretId).build()
    Try(secretsManagerClient.getSecretValue(secretValueRequest))
      .map(_.secretString())

  }
}
