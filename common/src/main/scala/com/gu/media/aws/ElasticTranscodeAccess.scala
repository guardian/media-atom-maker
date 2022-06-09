package com.gu.media.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder
import com.gu.media.Settings

trait ElasticTranscodeAccess { this: Settings with AwsAccess =>
  lazy val transcodePipelineId = getMandatoryString("aws.transcoder.pipelineId")

  lazy val transcoderClient = AmazonElasticTranscoderClientBuilder
    .standard()
    .withCredentials(credsProvider)
    .withRegion(region.getName)
    .build()
}
