package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadStatus(id: String, status: String, current: Option[Int], total: Option[Int], failed: Boolean, assetAdded: Boolean)

object UploadStatus {
  def apply(id: String, status: String, current: Int, total: Int, failed: Boolean = false, assetAdded: Boolean = false): UploadStatus = {
    UploadStatus(id, status, current = Some(current), total = Some(total), failed, assetAdded)
  }

  def indeterminate(id: String, status: String, failed: Boolean = false, assetAdded: Boolean = false): UploadStatus = {
    UploadStatus(id, status, current = None, total = None, failed, assetAdded)
  }

  implicit val format: Format[UploadStatus] = Jsonx.formatCaseClass[UploadStatus]
}
