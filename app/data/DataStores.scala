package data

import com.gu.atom.data._
import com.gu.atom.publish._
import com.gu.contentatom.thrift.Atom
import com.gu.media.aws.SNSAccess
import com.gu.media.iconik.{
  IconikCommission,
  IconikDynamoStore,
  IconikDynamoStoreWithParentIndex,
  IconikProject,
  IconikWorkingGroup,
  IndexConfig
}
import com.gu.media.pluto.{PlutoCommissionDataStore, PlutoProjectDataStore}
import com.gu.media.CapiAccess
import model.commands.CommandExceptions._
import util.{AWSConfig, NotifyingAtomPublisher}

class DataStores(aws: AWSConfig with SNSAccess, capi: CapiAccess) {

  val preview =
    new PreviewDynamoDataStoreV2(aws.dynamoDbSdkV2, aws.dynamoTableName)
  val published =
    new PublishedDynamoDataStoreV2(
      aws.dynamoDbSdkV2,
      aws.publishedDynamoTableName
    )

  val livePublisher: AtomPublisher = aws.capiContentEventsTopicName match {
    case Some(capiContentEventsTopicName) =>
      new NotifyingAtomPublisher(
        isLive = true,
        topicArn = capiContentEventsTopicName,
        underlying = new LiveKinesisAtomPublisherV2(
          aws.liveKinesisStreamName,
          aws.crossAccountKinesisClient
        ),
        sns = aws.snsClient
      )

    case None =>
      new LiveKinesisAtomPublisherV2(
        aws.liveKinesisStreamName,
        aws.crossAccountKinesisClient
      )
  }

  val previewPublisher: AtomPublisher = aws.capiContentEventsTopicName match {
    case Some(capiContentEventsTopicName) =>
      new NotifyingAtomPublisher(
        isLive = true,
        topicArn = capiContentEventsTopicName,
        underlying = new PreviewKinesisAtomPublisherV2(
          aws.previewKinesisStreamName,
          aws.crossAccountKinesisClient
        ),
        sns = aws.snsClient
      )

    case None =>
      new PreviewKinesisAtomPublisherV2(
        aws.previewKinesisStreamName,
        aws.crossAccountKinesisClient
      )
  }

  val reindexPreview: PreviewAtomReindexer =
    new PreviewKinesisAtomReindexerV2(
      aws.previewKinesisReindexStreamName,
      aws.crossAccountKinesisClient
    )

  val reindexPublished: PublishedKinesisAtomReindexerV2 =
    new PublishedKinesisAtomReindexerV2(
      aws.publishedKinesisReindexStreamName,
      aws.crossAccountKinesisClient
    )

  val plutoCommissionStore: PlutoCommissionDataStore =
    new PlutoCommissionDataStore(aws)
  val plutoProjectStore: PlutoProjectDataStore =
    new PlutoProjectDataStore(aws, plutoCommissionStore)

  private val iconikWorkingGroupStore =
    new IconikDynamoStore[IconikWorkingGroup](
      aws,
      aws.iconikWorkingGroupTableName
    )
  private val iconikCommissionStore =
    new IconikDynamoStoreWithParentIndex[IconikCommission](
      aws,
      aws.iconikCommissionTableName,
      IndexConfig("working-group-index", "workingGroupId")
    )
  private val iconikProjectStore =
    new IconikDynamoStoreWithParentIndex[IconikProject](
      aws,
      aws.iconikProjectTableName,
      IndexConfig("commission-index", "commissionId")
    )
  val iconikDataStore = new IconikStore(
    iconikProjectStore,
    iconikCommissionStore,
    iconikWorkingGroupStore
  )

  val atomListStore = AtomListStore(aws.stage, capi, preview)

}

trait UnpackedDataStores {
  val stores: DataStores

  // To avoid renaming references in code
  val previewDataStore: PreviewDataStore = stores.preview
  val publishedDataStore: PublishedDataStore = stores.published

  val previewPublisher: AtomPublisher = stores.previewPublisher
  val livePublisher: AtomPublisher = stores.livePublisher

  def getPreviewAtom(atomId: String): Atom =
    previewDataStore.getAtom(atomId) match {
      case Right(atom)      => atom
      case Left(IDNotFound) => AtomNotFound
      case Left(err)        => AtomDataStoreError(err.msg)
    }

  def getPublishedAtom(atomId: String): Atom =
    publishedDataStore.getAtom(atomId) match {
      case Right(atom)      => atom
      case Left(IDNotFound) => AtomNotFound
      case Left(err)        => AtomDataStoreError(err.msg)
    }

  def deletePreviewAtom(atomId: String): Unit =
    previewDataStore.deleteAtom(atomId) match {
      case Right(_)         =>
      case Left(IDNotFound) => AtomNotFound
      case Left(err)        => AtomDataStoreError(err.msg)
    }

  def deletePublishedAtom(atomId: String): Unit =
    publishedDataStore.deleteAtom(atomId) match {
      case Right(_)         =>
      case Left(IDNotFound) => /* Ignoring as atom not published */
      case Left(err)        => AtomDataStoreError(err.msg)
    }
}
