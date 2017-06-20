package com.gu.media.upload

import java.util.Date

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.atom.publish.PreviewKinesisAtomPublisher
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.AuditDataStore
import com.gu.media.aws.{DynamoAccess, KinesisAccess}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.VideoAsset
import com.gu.media.upload.model.Upload
import com.gu.media.util.MediaAtomHelpers._

import scala.util.control.NonFatal

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with DynamoAccess with KinesisAccess with Logging {
  private val store = new PreviewDynamoDataStore(dynamoDB, dynamoTableName)
  private val audit = new AuditDataStore(dynamoDB, auditDynamoTableName)
  private val publisher = new PreviewKinesisAtomPublisher(previewKinesisStreamName, crossAccountKinesisClient)

  override def handle(upload: Upload): Upload = {
    val atomId = upload.metadata.pluto.atomId
    val asset = upload.metadata.asset.getOrElse { throw new IllegalStateException("Missing asset") }

    val before = getAtom(atomId)
    val after = updateAtom(before)(addAsset(_, asset))

    saveAtom(after)
    audit.auditUpdate(atomId, "media-atom-pipeline", s"Added YouTube video $asset")

    upload
  }

  private def getAtom(id: String): Atom = {
    store.getAtom(id) match {
      case Right(atom) => atom
      case Left(err) => throw new IllegalStateException(s"${err.getMessage}. Cannot add asset", err)
    }
  }

  private def saveAtom(atom: Atom): Unit = {
    store.updateAtom(atom)

    val event = ContentAtomEvent(atom, EventType.Update, new Date().getTime)
    publisher.publishAtomEvent(event).recover { case NonFatal(e) => throw e }
  }
}
