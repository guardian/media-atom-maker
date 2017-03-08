package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadPart(id: Int, start: Long, end: Long, uploadedToS3: Long = 0, uploadedToYouTube: Long = 0)

object UploadPart {
  implicit val format: Format[UploadPart] = Jsonx.formatCaseClass[UploadPart]

  def build(size: Long): List[UploadPart] = {
    calculateChunks(size).zipWithIndex.map { case ((start, end), id) => UploadPart(id, start, end) }
  }

  def calculateChunks(size: Long): List[(Long, Long)] = {
    // We want gigabyte chunks. YouTube mandates that chunk size must be a multiple of 256Kb
    val gigabyte: Long = 1024 * 1024 * 1024
    val twoFiveSixKilobytes: Long = 1024 * 256

    var ret = List.empty[(Long, Long)]
    var start = 0L

    while((size - start) > gigabyte) {
      val end = start + gigabyte

      ret :+= (start, end)
      start += gigabyte
    }

    val lastSize = (size - start) % twoFiveSixKilobytes
    val lastStart = size - lastSize

    ret :+= (start, lastStart)
    ret :+= (lastStart, size)

    ret
  }
}
