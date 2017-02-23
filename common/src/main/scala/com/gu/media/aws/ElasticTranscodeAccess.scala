package com.gu.media.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient

trait ElasticTranscodeAccess { this: AwsAccess =>
  lazy val transcodePipelineId = getMandatoryString("aws.transcoder.pipelineId")

  lazy val transcoderClient = region.createClient(
    classOf[AmazonElasticTranscoderClient],
    credsProvider,
    null
  )
}
