package com.gu.media

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

package object upload {
  // Stored in the progress table
  case class UploadEntry(id: String, atomId: String, parts: List[UploadPartEntry])
  case class UploadPartEntry(start: Long, end: Long, uploadedToS3: Long = 0, uploadedToYouTube: Long = 0)

  // Temporary STS-issued credentials for uploading video parts.
  // NOT STORED IN THE PROGRESS TABLE.
  case class UploadCredentials(temporaryAccessId: String, temporarySecretKey: String, sessionToken: String)

  // Front-end API
  case class CreateAPIRequest(atomId: String, filename: String, size: Long)
  case class APIPart(id: Int, key: String, start: Long, end: Long)
  case class CreateAPIResponse(id: String, region: String, bucket: String, parts: List[APIPart])

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

  implicit val uploadPartEntryJsonFormat: Format[UploadPartEntry] = Jsonx.formatCaseClass[UploadPartEntry]
  implicit val uploadEntryJsonFormat: Format[UploadEntry] = Jsonx.formatCaseClass[UploadEntry]
  implicit val uploadCredentialsJsonFormat: Format[UploadCredentials] = Jsonx.formatCaseClass[UploadCredentials]
  implicit val createRequestJsonFormat: Format[CreateAPIRequest] = Jsonx.formatCaseClass[CreateAPIRequest]
  implicit val apiPartJsonFormat: Format[APIPart] = Jsonx.formatCaseClass[APIPart]
  implicit val createResponseJsonFormat: Format[CreateAPIResponse] = Jsonx.formatCaseClass[CreateAPIResponse]
}
