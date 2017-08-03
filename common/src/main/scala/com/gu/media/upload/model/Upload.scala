package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

// All data is conceptually immutable except UploadProgress
case class Upload(id: String, parts: List[UploadPart], metadata: UploadMetadata, progress: UploadProgress)

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

  def mergeProgress(upload: Upload, progress: UploadProgress): Upload = {
    upload.copy(progress = UploadProgress(
      retries = Math.max(upload.progress.retries, progress.retries),
      chunksInS3 = Math.max(upload.progress.chunksInS3, progress.chunksInS3),
      chunksInYouTube = Math.max(upload.progress.chunksInYouTube, progress.chunksInYouTube),
      fullyUploaded = upload.progress.fullyUploaded || progress.fullyUploaded,
      fullyTranscoded = upload.progress.fullyTranscoded || progress.fullyTranscoded
    ))
  }

  private def chunksOfExactly(chunkSize: Long, size: Long): (List[(Long, Long)], Long) = {
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
