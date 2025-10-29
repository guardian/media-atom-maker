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
import com.gu.media.{CapiAccess, PlutoDataStore}
import model.commands.CommandExceptions._
import util.{AWSConfig, NotifyingAtomPublisher}

class DataStores(aws: AWSConfig with SNSAccess, capi: CapiAccess) {

  val preview = new PreviewDynamoDataStore(aws.dynamoDB, aws.dynamoTableName)
  val published =
    new PublishedDynamoDataStore(aws.dynamoDB, aws.publishedDynamoTableName)

  val pluto: PlutoDataStore =
    new PlutoDataStore(aws.scanamo, aws.manualPlutoDynamo)

  val livePublisher: AtomPublisher = aws.capiContentEventsTopicName match {
    case Some(capiContentEventsTopicName) =>
      new NotifyingAtomPublisher(
        isLive = true,
        topicArn = capiContentEventsTopicName,
        underlying = new LiveKinesisAtomPublisher(
          aws.liveKinesisStreamName,
          aws.crossAccountKinesisClient
        ),
        sns = aws.snsClient
      )

    case None =>
      new LiveKinesisAtomPublisher(
        aws.liveKinesisStreamName,
        aws.crossAccountKinesisClient
      )
  }

  val previewPublisher: AtomPublisher = aws.capiContentEventsTopicName match {
    case Some(capiContentEventsTopicName) =>
      new NotifyingAtomPublisher(
        isLive = true,
        topicArn = capiContentEventsTopicName,
        underlying = new PreviewKinesisAtomPublisher(
          aws.previewKinesisStreamName,
          aws.crossAccountKinesisClient
        ),
        sns = aws.snsClient
      )

    case None =>
      new PreviewKinesisAtomPublisher(
        aws.previewKinesisStreamName,
        aws.crossAccountKinesisClient
      )
  }

  val reindexPreview: PreviewAtomReindexer =
    new PreviewKinesisAtomReindexer(
      aws.previewKinesisReindexStreamName,
      aws.crossAccountKinesisClient
    )

  val reindexPublished: PublishedKinesisAtomReindexer =
    new PublishedKinesisAtomReindexer(
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
