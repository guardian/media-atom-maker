package com.gu.media.upload

import java.net.URLEncoder
import java.util.Date

import com.gu.atom.data.PreviewDynamoDataStore
import com.gu.atom.publish.PreviewKinesisAtomPublisher
import com.gu.contentatom.thrift.atom.media.MediaAtom
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.AuditDataStore
import com.gu.media.aws.{DynamoAccess, KinesisAccess, UploadAccess}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{SelfHostedAsset, YouTubeAsset}
import com.gu.media.upload.model.Upload
import com.gu.media.util.MediaAtomHelpers
import com.gu.media.util.MediaAtomHelpers._

import scala.util.control.NonFatal

class AddAssetToAtom extends LambdaWithParams[Upload, Upload] with DynamoAccess with KinesisAccess with UploadAccess with Logging {
  private val selfHostedOrigin: String = getMandatoryString("aws.upload.selfHostedOrigin")

  private val store = new PreviewDynamoDataStore(dynamoDB, dynamoTableName)
  private val audit = new AuditDataStore(dynamoDB, auditDynamoTableName)
  private val publisher = new PreviewKinesisAtomPublisher(previewKinesisStreamName, crossAccountKinesisClient)

  override def handle(upload: Upload): Upload = {
    val atomId = upload.metadata.pluto.atomId
    val asset = getAsset(upload)
    val before = getAtom(atomId)

    val after = updateAtom(before) { mediaAtom =>
      val version = upload.metadata.version.getOrElse(MediaAtomHelpers.getNextAssetVersion(mediaAtom))

      addAsset(mediaAtom, asset, version)
    }

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

  private def getAsset(upload: Upload) = {
    upload.metadata.asset match {
      case Some(asset: YouTubeAsset) =>
        asset

      case Some(SelfHostedAsset(sources)) =>
        SelfHostedAsset(sources.map { source =>
          source.copy(src = s"$selfHostedOrigin/${URLEncoder.encode(source.src, "UTF-8")}")
        })

      case None =>
        throw new IllegalStateException("Missing asset")
    }
  }
}
