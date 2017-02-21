package data

import com.gu.atom.data.{PreviewDataStore, PreviewDynamoDataStore, PublishedDataStore, PublishedDynamoDataStore}
import com.gu.atom.publish._
import com.gu.contentatom.thrift.AtomData
import com.gu.contentatom.thrift.atom.media.MediaAtom
import util.atom.MediaAtomImplicits
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import util.AWSConfig

class DataStores(aws: AWSConfig) extends MediaAtomImplicits {
  val published: PublishedDataStore = createPublished()
  val preview: PreviewDataStore = createPreview()
  val audit: AuditDataStore = new AuditDataStore(aws.dynamoDB, aws.auditDynamoTableName)

  val livePublisher: LiveKinesisAtomPublisher =
    new LiveKinesisAtomPublisher(aws.liveKinesisStreamName, aws.kinesisClient)

  val previewPublisher: PreviewKinesisAtomPublisher =
    new PreviewKinesisAtomPublisher(aws.previewKinesisStreamName, aws.kinesisClient)

  val reindexPreview: PreviewAtomReindexer =
    new PreviewKinesisAtomReindexer(aws.previewKinesisReindexStreamName, aws.kinesisClient)

  val reindexPublished: PublishedKinesisAtomReindexer =
    new PublishedKinesisAtomReindexer(aws.publishedKinesisReindexStreamName, aws.kinesisClient)

  private def createPublished() = {
    new PublishedDynamoDataStore[MediaAtom](aws.dynamoDB, aws.publishedDynamoTableName) {
      def fromAtomData = { case AtomData.Media(data) => data }
      def toAtomData(data: MediaAtom) = AtomData.Media(data)
    }
  }

  private def createPreview() = {
    new PreviewDynamoDataStore[MediaAtom](aws.dynamoDB, aws.dynamoTableName) {
      def fromAtomData = { case AtomData.Media(data) => data }
      def toAtomData(data: MediaAtom) = AtomData.Media(data)
    }
  }
}

trait HasDataStores {
  // To avoid renaming references in code
  val stores: DataStores

  val previewDataStore: PreviewDataStore = stores.preview
  val publishedDataStore: PublishedDataStore = stores.published
  val auditDataStore: AuditDataStore = stores.audit

  val previewPublisher: PreviewAtomPublisher = stores.previewPublisher
  val livePublisher: LiveAtomPublisher = stores.livePublisher
}
