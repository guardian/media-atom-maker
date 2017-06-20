package com.gu.media.upload.model

import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

case class UploadStatus(id: String, status: String, asset: Option[String], current: Option[Int], total: Option[Int], failed: Boolean)

object UploadStatus {
  def indeterminate(id: String, status: String, asset: Option[String] = None, failed: Boolean = false): UploadStatus = {
    UploadStatus(id, status, asset, current = None, total = None, failed)
  }

  implicit val format: Format[UploadStatus] = Jsonx.formatCaseClass[UploadStatus]
}
