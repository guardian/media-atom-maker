package data

import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.media.upload.UploadsDataStore
import com.gu.scanamo.DynamoFormat
import util.atom.MediaAtomImplicits
import com.gu.scanamo.scrooge.ScroogeDynamoFormat._
import model.commands.CommandExceptions._
import util.AWSConfig

import scala.reflect.ClassTag

class DataStores(aws: AWSConfig) extends MediaAtomImplicits {
  import cats.syntax.either._ // appears unused but is required to make the data stores compile

  val mediaDynamoFormats = new AtomDynamoFormats[MediaAtom] {
    def fromAtomData: PartialFunction[AtomData, MediaAtom] = { case AtomData.Media(data) => data }
    def toAtomData(data: MediaAtom): AtomData = AtomData.Media(data)
  }

  val preview: PreviewDynamoDataStore[MediaAtom] = getPreview(mediaDynamoFormats)
  val published: PublishedDynamoDataStore[MediaAtom] = getPublished(mediaDynamoFormats)
  val audit: AuditDataStore = new AuditDataStore(aws.dynamoDB, aws.auditDynamoTableName)
  val pluto: PlutoDataStore = new PlutoDataStore(aws.dynamoDB, aws.manualPlutoDynamo)

  val livePublisher: LiveKinesisAtomPublisher =
    new LiveKinesisAtomPublisher(aws.liveKinesisStreamName, aws.crossAccountKinesisClient)

  val previewPublisher: PreviewKinesisAtomPublisher =
    new PreviewKinesisAtomPublisher(aws.previewKinesisStreamName, aws.crossAccountKinesisClient)

  val reindexPreview: PreviewAtomReindexer =
    new PreviewKinesisAtomReindexer(aws.previewKinesisReindexStreamName, aws.crossAccountKinesisClient)

  val reindexPublished: PublishedKinesisAtomReindexer =
    new PublishedKinesisAtomReindexer(aws.publishedKinesisReindexStreamName, aws.crossAccountKinesisClient)

  val uploadStore: UploadsDataStore = new UploadsDataStore(aws)

  private def getPreview[T: ClassTag: DynamoFormat](dynamoFormats: AtomDynamoFormats[T]): PreviewDynamoDataStore[T] = {
    new PreviewDynamoDataStore[T](aws.dynamoDB, aws.dynamoTableName) {
      def fromAtomData = dynamoFormats.fromAtomData
      def toAtomData(data: T) = dynamoFormats.toAtomData(data)
    }
  }

  private def getPublished[T: ClassTag: DynamoFormat](dynamoFormats: AtomDynamoFormats[T]): PublishedDynamoDataStore[T] = {
    new PublishedDynamoDataStore[T](aws.dynamoDB, aws.publishedDynamoTableName) {
      def fromAtomData = dynamoFormats.fromAtomData
      def toAtomData(data: T) = dynamoFormats.toAtomData(data)
    }
  }
}

trait UnpackedDataStores {
  val stores: DataStores

  // To avoid renaming references in code
  val previewDataStore: PreviewDataStore = stores.preview
  val publishedDataStore: PublishedDataStore = stores.published
  val auditDataStore: AuditDataStore = stores.audit
  val plutoDataStore: PlutoDataStore = stores.pluto

  val previewPublisher: PreviewAtomPublisher = stores.previewPublisher
  val livePublisher: LiveAtomPublisher = stores.livePublisher

  def getPreviewAtom(atomId: String): Atom  = previewDataStore.getAtom(atomId) match {
    case Right(atom) => atom
    case Left(IDNotFound) => AtomNotFound
    case Left(err) => AtomDataStoreError(err.msg)
  }

  def getPublishedAtom(atomId: String): Atom  = publishedDataStore.getAtom(atomId) match {
    case Right(atom) => atom
    case Left(IDNotFound) => AtomNotFound
    case Left(err) => AtomDataStoreError(err.msg)
  }

  def deletePreviewAtom(atomId: String): Unit = previewDataStore.deleteAtom(atomId) match {
    case Right(_) =>
    case Left(IDNotFound) => AtomNotFound
    case Left(err) => AtomDataStoreError(err.msg)
  }

  def deletePublishedAtom(atomId: String): Unit = publishedDataStore.deleteAtom(atomId) match {
    case Right(_) =>
    case Left(IDNotFound) => /* Ignoring as atom not published */
    case Left(err) => AtomDataStoreError(err.msg)
  }
}
