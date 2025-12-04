package util

import java.time.Instant
import com.gu.media.aws.{AwsAccess, UploadAccess}
import com.gu.media.model.{
  MediaAtom,
  PlutoSyncMetadataMessage,
  SelfHostedAsset,
  VideoSource
}
import com.gu.media.upload.{TranscoderOutputKey, UploadPartKey}
import com.gu.media.upload.model._
import model.commands.CommandExceptions.AtomMissingYouTubeChannel

object UploadBuilder {
  def build(
      atom: MediaAtom,
      email: String,
      assetVersion: Long,
      request: UploadRequest,
      aws: AwsAccess with UploadAccess
  ): Upload = {
    val id = s"${atom.id}-$assetVersion"

    val plutoData = PlutoSyncMetadataMessage.build(id, atom, aws, email)

    val metadata = UploadMetadata(
      user = email,
      bucket = aws.userUploadBucket,
      region = aws.awsV2Region.id(),
      title = atom.title,
      pluto = plutoData,
      selfHost = request.selfHost,
      runtime = getRuntimeMetadata(request.selfHost, atom.channelId),
      asset = getAsset(
        request.selfHost,
        atom.title,
        atom.id,
        assetVersion,
        subtitleVersion = 0
      ),
      originalFilename = Some(request.filename),
      version = Some(assetVersion),
      startTimestamp = Some(currentTimestamp)
    )

    val progress = UploadProgress(
      chunksInS3 = 0,
      chunksInYouTube = 0,
      fullyUploaded = false,
      fullyTranscoded = false,
      retries = 0
    )

    val parts = chunk(id, request.size, aws)

    Upload(id, parts, metadata, progress)
  }

  /** Prepare an existing upload to be re-run in the state machine so that
    * subtitles can be added or removed
    * @param upload
    * @return
    */
  def buildForSubtitleChange(
      upload: Upload,
      newSubtitleSource: Option[VideoSource]
  ): Upload = {
    val assetVersion = upload.metadata.version.getOrElse(1L)
    val subtitleVersion = Upload.getNextSubtitleVersion(upload)
    val updatedAsset = getAsset(
      upload.metadata.selfHost,
      upload.metadata.title,
      upload.metadata.pluto.atomId,
      assetVersion,
      subtitleVersion
    )
    upload.copy(
      metadata = upload.metadata.copy(
        asset = updatedAsset,
        subtitleSource = newSubtitleSource,
        subtitleVersion = Some(subtitleVersion),
        startTimestamp = Some(currentTimestamp)
      ),
      progress = upload.progress.copy(fullyTranscoded = false)
    )
  }

  private[util] def currentTimestamp: Long = Instant.now().toEpochMilli

  private def getAsset(
      selfHosted: Boolean,
      title: String,
      atomId: String,
      assetVersion: Long,
      subtitleVersion: Long,
      includeMp4: Boolean = true,
      includeM3u8: Boolean = true
  ): Option[SelfHostedAsset] = {
    if (!selfHosted) {
      // YouTube assets are added after they have been uploaded (once we know the ID)
      None
    } else {
      // mp4 output doesn't change with subtitle processing, so s3 key stays at subtitle version 0
      val mp4Key =
        TranscoderOutputKey(title, atomId, assetVersion, 0, "mp4").toString
      val mp4Source =
        if (includeMp4) Some(VideoSource(mp4Key, VideoSource.mimeTypeMp4))
        else None

      // m3u8 output changes when subtitles are processed, so s3 key includes a subtitle version
      val m3u8Key = TranscoderOutputKey(
        title,
        atomId,
        assetVersion,
        subtitleVersion,
        "m3u8"
      ).toString
      val m3u8Source =
        if (includeM3u8) Some(VideoSource(m3u8Key, VideoSource.mimeTypeM3u8))
        else None
      val sources = mp4Source ++ m3u8Source
      Some(SelfHostedAsset(sources.toList))
    }
  }

  private def getRuntimeMetadata(
      selfHosted: Boolean,
      atomChannel: Option[String]
  ) = atomChannel match {
    case _ if selfHosted => SelfHostedUploadMetadata(List.empty)
    case Some(channel)   => YouTubeUploadMetadata(channel, uri = None)
    case None            => AtomMissingYouTubeChannel
  }

  private def chunk(
      uploadId: String,
      size: Long,
      aws: UploadAccess
  ): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(
        UploadPartKey(aws.userUploadFolder, uploadId, id).toString,
        start,
        end
      )
    }
  }
}
