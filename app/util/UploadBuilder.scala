package util

import java.time.Instant

import com.gu.media.aws.{AwsAccess, UploadAccess}
import com.gu.media.model.{MediaAtom, PlutoSyncMetadataMessage, SelfHostedAsset, VideoSource}
import com.gu.media.upload.{TranscoderOutputKey, UploadPartKey}
import com.gu.media.upload.model._
import model.commands.CommandExceptions.AtomMissingYouTubeChannel

object UploadBuilder {
  def build(atom: MediaAtom, email: String, version: Long, request: UploadRequest, aws: AwsAccess with UploadAccess): Upload = {
    val id = s"${atom.id}-$version"

    val plutoData = PlutoSyncMetadataMessage.build(id, atom, aws, email)

    val metadata = UploadMetadata(
      user = email,
      bucket = aws.userUploadBucket,
      region = aws.region.getName,
      title = atom.title,
      pluto = plutoData,
      selfHost = request.selfHost,
      runtime = getRuntimeMetadata(request.selfHost, atom.channelId),
      asset = getAsset(request.selfHost, atom.title, id),
      originalFilename = Some(request.filename),
      version = Some(version),
      startTimestamp = Some(Instant.now().toEpochMilli)
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

  private def getAsset(selfHosted: Boolean, title: String, id: String): Option[SelfHostedAsset] = {
    if(!selfHosted) {
      // YouTube assets are added after they have been uploaded (once we know the ID)
      None
    } else {
      val mp4Key = TranscoderOutputKey(title, id, "mp4").toString
      val mp4Source = VideoSource(mp4Key, "video/mp4")

      Some(SelfHostedAsset(List(mp4Source)))
    }
  }

  private def getRuntimeMetadata(selfHosted: Boolean, atomChannel: Option[String]) = atomChannel match {
    case _ if selfHosted => SelfHostedUploadMetadata(List.empty)
    case Some(channel) => YouTubeUploadMetadata(channel, uri = None)
    case None => AtomMissingYouTubeChannel
  }

  private def chunk(uploadId: String, size: Long, aws: UploadAccess): List[UploadPart] = {
    val boundaries = Upload.calculateChunks(size)

    boundaries.zipWithIndex.map { case ((start, end), id) =>
      UploadPart(UploadPartKey(aws.userUploadFolder, uploadId, id).toString, start, end)
    }
  }
}
