package com.gu.media.upload.model

import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import com.gu.media.upload.UploadUri
import play.api.libs.json.Format

case class WaitOnUpload(input: Upload, taskToken: String, executionId: String)

object WaitOnUpload {
  implicit val format: Format[WaitOnUpload] = Jsonx.formatCaseClass[WaitOnUpload]
}
// All data is conceptually immutable except UploadProgress
case class Upload(
    id: String,
    parts: List[UploadPart],
    metadata: UploadMetadata,
    progress: UploadProgress
)

object Upload {
  implicit val format: Format[Upload] = Jsonx.formatCaseClass[Upload]

  // We want 100MB chunks to upload in a single lambda invocation
  val oneHundredMegabytes: Long = 100 * 1024 * 1024

  def calculateChunks(size: Long): List[(Long, Long)] = {
    chunksOfExactly(oneHundredMegabytes, size) match {
      case (bigChunks, 0) =>
        bigChunks

      case (bigChunks, remainder) =>
        val start = startOfLastChunk(bigChunks)
        bigChunks :+ (start, start + remainder)
    }
  }

  def videoInputUri(upload: Upload): UploadUri =
    UploadUri(upload.metadata.bucket, upload.metadata.pluto.s3Key)

  def subtitleInputUri(upload: Upload): Option[UploadUri] =
    upload.metadata.subtitleSource.map(s =>
      UploadUri(upload.metadata.bucket, s.src)
    )

  def getCurrentSubtitleVersion(upload: Upload): Long =
    upload.metadata.subtitleVersion.getOrElse(0L)

  def getNextSubtitleVersion(upload: Upload): Long =
    upload.metadata.subtitleVersion.map(v => v + 1).getOrElse(1L)

  private def chunksOfExactly(
      chunkSize: Long,
      size: Long
  ): (List[(Long, Long)], Long) = {
    val numParts = (size / chunkSize).toInt
    val remainder = size % chunkSize

    val chunks = (0 until numParts).toList.map { n =>
      val chunkStart = n * chunkSize
      val chunkEnd = chunkStart + chunkSize

      (chunkStart, chunkEnd)
    }

    (chunks, remainder)
  }

  private def startOfLastChunk(bigChunks: List[(Long, Long)]): Long = {
    bigChunks.lastOption.map(_._2).getOrElse(0)
  }
}
