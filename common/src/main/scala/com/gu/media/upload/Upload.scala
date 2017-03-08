package com.gu.media.upload

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class Upload(id: String, atomId: String, user: String, bucket: String, region: String, parts: List[UploadPart])

object Upload {
  implicit val format: Format[Upload] = Jsonx.formatCaseClass[Upload]

  def calculateChunks(size: Long): List[(Long, Long)] = {
    // We want 100MB chunks. YouTube mandates that chunk size must be a multiple of 256KB
    val oneHundredMegabytes: Long = 100 * 1024 * 1024
    val twoFiveSixKilobytes: Long = 1024 * 256

    var ret = List.empty[(Long, Long)]
    var start = 0L

    while((size - start) > oneHundredMegabytes) {
      val end = start + oneHundredMegabytes

      ret :+= (start, end)
      start += oneHundredMegabytes
    }

    val lastSize = (size - start) % twoFiveSixKilobytes
    val lastStart = size - lastSize

    ret :+= (start, lastStart)
    ret :+= (lastStart, size)

    ret
  }
}
