package data

import javax.inject.{Inject, Provider}

import com.gu.atom.publish._
import util.AWSConfig

class LiveAtomPublisherProvider @Inject() (awsConfig: AWSConfig)
  extends Provider[LiveAtomPublisher] {
    def get() = new LiveKinesisAtomPublisher(awsConfig.liveKinesisStreamName, awsConfig.kinesisClient)
}

class PreviewAtomPublisherProvider @Inject() (awsConfig: AWSConfig)
  extends Provider[PreviewAtomPublisher] {
    def get() = new PreviewKinesisAtomPublisher(awsConfig.previewKinesisStreamName, awsConfig.kinesisClient)
}
