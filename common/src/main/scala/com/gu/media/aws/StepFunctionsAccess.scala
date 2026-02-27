package com.gu.media.aws

import com.gu.media.Settings
import software.amazon.awssdk.services.sfn.SfnClient

trait StepFunctionsAccess { this: AwsAccess =>
  lazy val stepFunctionsClient = SfnClient
    .builder()
    .credentialsProvider(credentials.instance.awsV2Creds)
    .region(awsV2Region)
    .build()
}
