package com.gu.media.upload

import java.util.Date
import com.gu.atom.data.PreviewDynamoDataStoreV2
import com.gu.atom.publish.PreviewKinesisAtomPublisherV2

import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.aws.{DynamoAccess, KinesisAccess, UploadAccess}
import com.gu.media.lambda.LambdaWithParams
import com.gu.media.logging.Logging
import com.gu.media.model.{
  AuditMessage,
  SelfHostedAsset,
  VideoAsset,
  YouTubeAsset
}
import com.gu.media.upload.model.Upload
import com.gu.media.util.MediaAtomHelpers
import com.gu.media.util.MediaAtomHelpers._

import scala.util.control.NonFatal

class AddAssetToAtom
    extends LambdaWithParams[Upload, Upload]
    with DynamoAccess
    with KinesisAccess
    with UploadAccess
    with Logging {
  private val selfHostedOrigin: String = getMandatoryString(
    "aws.upload.selfHostedOrigin"
  )

  private val store =
    new PreviewDynamoDataStoreV2(dynamoDbSdkV2, dynamoTableName)
  private val publisher = new PreviewKinesisAtomPublisherV2(
    previewKinesisStreamName,
    crossAccountKinesisClient
  )

  override def handle(upload: Upload): Upload = {
    val atomId = upload.metadata.pluto.atomId
    val asset = getAsset(upload)
    val before = getAtom(atomId)
    val user = getUser(upload.metadata.user)

    val after = updateAtom(before, user) { mediaAtom =>
      val assetVersion = upload.metadata.version.getOrElse(
        MediaAtomHelpers.getNextAssetVersion(mediaAtom)
      )

      addAsset(mediaAtom, asset, assetVersion)
    }

    saveAtom(after)
    AuditMessage(
      atomId,
      "Update",
      "media-atom-pipeline",
      Some(s"Added YouTube video $asset")
    ).logMessage()

    upload
  }

  private def getAtom(id: String): Atom = {
    store.getAtom(id) match {
      case Right(atom) => atom
      case Left(err) =>
        throw new IllegalStateException(
          s"${err.getMessage}. Cannot add asset",
          err
        )
    }
  }

  private def saveAtom(atom: Atom): Unit = {
    store.updateAtom(atom)

    val event = ContentAtomEvent(atom, EventType.Update, new Date().getTime)
    publisher.publishAtomEvent(event).recover { case NonFatal(e) => throw e }
  }

  private def getAsset(upload: Upload): VideoAsset = {
    upload.metadata.asset match {
      case Some(asset: YouTubeAsset) =>
        asset

      case Some(asset: SelfHostedAsset) =>
        MediaAtomHelpers.urlEncodeSources(asset, selfHostedOrigin)

      case None =>
        throw new IllegalStateException("Missing asset")
    }
  }
}
