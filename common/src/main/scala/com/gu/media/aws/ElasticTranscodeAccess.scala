package com.gu.media.aws

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClient
import com.gu.media.Settings

trait ElasticTranscodeAccess { this: Settings with AwsAccess =>
  lazy val transcodePipelineId = getMandatoryString("aws.transcoder.pipelineId")

  lazy val transcoderClient = region.createClient(
    classOf[AmazonElasticTranscoderClient],
    credsProvider,
    null
  )
}
