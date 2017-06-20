package com.gu.media.upload.model

import com.gu.media.model.VideoAsset
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadStatus(id: String, status: String, asset: Option[VideoAsset], current: Option[Int], total: Option[Int], failed: Boolean)

object UploadStatus {
  def indeterminate(id: String, status: String): UploadStatus = {
    UploadStatus(id, status, asset = None, current = None, total = None, failed = false)
  }

  implicit val format: Format[UploadStatus] = Jsonx.formatCaseClass[UploadStatus]
}
