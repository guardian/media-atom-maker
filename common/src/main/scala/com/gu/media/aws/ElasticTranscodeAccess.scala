package com.gu.media.aws

import com.gu.media.Settings
import software.amazon.awssdk.services.elastictranscoder.{
  ElasticTranscoderClient,
  ElasticTranscoderClientBuilder
}

trait ElasticTranscodeAccess { this: Settings with AwsAccess =>
  lazy val transcodePipelineId = getMandatoryString("aws.transcoder.pipelineId")

  lazy val transcoderClient = ElasticTranscoderClient
    .builder()
    .region(awsV2Region)
    .credentialsProvider(credentials.instance.awsV2Creds)
    .build()
}
