package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadStatus(id: String, status: String, current: Int, total: Int, failed: Boolean)

object UploadStatus {
  def indeterminate(id: String, status: String): UploadStatus = {
    UploadStatus(id, status, current = -1, total = -1, failed = false)
  }

  implicit val format: Format[UploadStatus] = Jsonx.formatCaseClass[UploadStatus]
}
