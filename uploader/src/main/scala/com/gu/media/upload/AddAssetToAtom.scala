package com.gu.media.upload

import java.util.Date
import com.gu.atom.data.PreviewDynamoDataStoreV2
import com.gu.atom.publish.PreviewKinesisAtomPublisherV2
import com.gu.contentatom.thrift.atom.media.{
  AssetType,
  Asset => ThriftAsset,
  Platform => ThriftPlatform
}
import com.gu.contentatom.thrift.{
  ImageAssetDimensions => ThriftImageAssetDimensions
}
import com.gu.contentatom.thrift.{Atom, ContentAtomEvent, EventType}
import com.gu.media.aws.{DynamoAccess, KinesisAccess, UploadAccess}
import com.gu.media.lambda.{LambdaBase, LambdaWithParams}
import com.gu.media.logging.Logging
import com.gu.media.model.{AuditMessage, VideoSource, YouTubeAsset}
import com.gu.media.upload.mediaconvert.{
  JobSettingsBuilder,
  OutputGroupDefinition
}
import com.gu.media.upload.model.{
  MediaConvertEvent,
  MediaConvertOutputDetails,
  MediaConvertOutputGroupDetails,
  SelfHostedUploadMetadata,
  Upload
}
import com.gu.media.util.AspectRatio
import com.gu.media.util.MediaAtomHelpers._

import java.net.URI
import scala.util.control.NonFatal

class AddAssetToAtom
    extends LambdaWithParams[Upload, Upload]
    with LambdaBase
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
    val before = getAtom(atomId)
    val user = getUser(upload.metadata.user)

    val after = updateAtom(before, user) { mediaAtom =>
      val assetVersion = upload.metadata.version.getOrElse(
        getNextAssetVersion(mediaAtom)
      )

      val asset = getAsset(upload, assetVersion)

      addAssets(
        mediaAtom,
        asset,
        assetVersion
      )
    }

    saveAtom(after)
    AuditMessage(
      atomId,
      "Update",
      "media-atom-pipeline",
      Some(s"Added video asset")
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

  private def getAsset(
      upload: Upload,
      version: Long
  ): List[ThriftAsset] = {
    (upload.metadata.asset, upload.metadata.runtime) match {
      case (Some(asset: YouTubeAsset), _) =>
        assetsFromYoutubeEvent(asset, version)

      case (_, SelfHostedUploadMetadata(_, Some(completeEvent))) =>
        assetsFromCompleteEvent(completeEvent, version)

      case _ =>
        throw new IllegalStateException("Missing asset")
    }
  }

  private def assetsFromYoutubeEvent(asset: YouTubeAsset, version: Long) = {
    List(
      ThriftAsset(
        AssetType.Video,
        version,
        asset.id,
        ThriftPlatform.Youtube,
        mimeType = None
      )
    )
  }

  private def assetsFromCompleteEvent(
      completeEvent: MediaConvertEvent,
      version: Long
  ): List[ThriftAsset] = {
    val outputGroupsSettings = JobSettingsBuilder.outputGroups
    val outputGroupsResults = completeEvent.detail.outputGroupDetails

    outputGroupAssets(outputGroupsSettings, outputGroupsResults, version) ++
      outputAssets(outputGroupsSettings, outputGroupsResults, version)
  }

  private def outputGroupAssets(
      outputGroupsSettings: List[OutputGroupDefinition],
      outputGroupsResults: List[MediaConvertOutputGroupDetails],
      version: Long
  ) =
    for {
      (settings, results) <- outputGroupsSettings.zip(outputGroupsResults)
      assetType <- settings.assetType
      mimeType <- settings.mimeType
      playlistFilePath <- first(results.playlistFilePaths)
    } yield ThriftAsset(
      assetType,
      version,
      urlEncodeSource(
        new URI(playlistFilePath).getPath.drop(1),
        selfHostedOrigin
      ),
      ThriftPlatform.Url,
      Some(mimeType),
      // HLS playlists can include multiple renditions with different resolutions, so we can't provide dimensions for the asset at this level
      dimensions = None,
      aspectRatio = None
    )

  private def outputAssets(
      outputGroupsSettings: List[OutputGroupDefinition],
      outputGroupsResults: List[MediaConvertOutputGroupDetails],
      version: Long
  ) =
    for {
      (outputGroupSettings, outputGroupResults) <- outputGroupsSettings.zip(
        outputGroupsResults
      )
      outputsSettings = outputGroupSettings.outputs
      outputsResults = outputGroupResults.outputDetails
      (outputSettings, outputResults) <- outputsSettings.zip(outputsResults)
      filePath <- first(outputResults.outputFilePaths)
      assetType <- outputSettings.assetType
      mimeType <- outputSettings.mimeType
    } yield ThriftAsset(
      assetType,
      version,
      urlEncodeSource(
        new URI(correctFilepath(filePath, mimeType)).getPath.drop(1),
        selfHostedOrigin
      ),
      ThriftPlatform.Url,
      Some(mimeType),
      dimensions(outputResults),
      ratio(outputResults)
    )

  private def first[T](collection: Option[List[T]]) =
    collection.flatMap(_.headOption)

  private def first[T](collection: List[T]) =
    collection.headOption

  // MediaConvert events don't include the .vtt extension for subtitle files event even though the object in S3 has this extension
  private def correctFilepath(filePath: String, mimeType: String) = {
    if (mimeType == VideoSource.mimeTypeVtt && !filePath.endsWith(".vtt")) {
      filePath + ".vtt"
    } else filePath
  }

  private def ratio(outputDetails: MediaConvertOutputDetails) = {
    for {
      videoDetails <- outputDetails.videoDetails
      h = videoDetails.heightInPx
      w = videoDetails.widthInPx
      ratio <- AspectRatio.calculate(w, h)
    } yield ratio.name
  }

  private def dimensions(outputDetails: MediaConvertOutputDetails) = {
    val dimensions = for {
      videoDetails <- outputDetails.videoDetails
      h = videoDetails.heightInPx
      w = videoDetails.widthInPx
    } yield ThriftImageAssetDimensions(h, w)
    dimensions
  }
}
