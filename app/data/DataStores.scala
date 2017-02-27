package data

import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import util.atom.MediaAtomImplicits
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import model.commands.CommandExceptions._
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

trait UnpackedDataStores {
  val stores: DataStores

  // To avoid renaming references in code
  val previewDataStore: PreviewDataStore = stores.preview
  val publishedDataStore: PublishedDataStore = stores.published
  val auditDataStore: AuditDataStore = stores.audit

  val previewPublisher: PreviewAtomPublisher = stores.previewPublisher
  val livePublisher: LiveAtomPublisher = stores.livePublisher

  def getPreviewAtom(atomId: String): Atom  = previewDataStore.getAtom(atomId) match {
    case Right(atom) => atom
    case Left(IDNotFound) => AtomNotFound
    case Left(err) => AtomDataStoreError(err.msg)
  }

  def getPublishedAtom(atomId: String): Atom  = previewDataStore.getAtom(atomId) match {
    case Right(atom) => atom
    case Left(IDNotFound) => AtomNotFound
    case Left(err) => AtomDataStoreError(err.msg)
  }
}
