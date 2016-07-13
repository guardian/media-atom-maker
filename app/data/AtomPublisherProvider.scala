package data

import javax.inject.{Inject, Provider}

import com.gu.atom.publish.{KinesisAtomPublisher, AtomPublisher}
import util.AWSConfig

class AtomPublisherProvider @Inject() (awsConfig: AWSConfig)
  extends Provider[AtomPublisher] {
    def get() = new KinesisAtomPublisher(awsConfig.kinesisStreamName, awsConfig.kinesisClient)
}
