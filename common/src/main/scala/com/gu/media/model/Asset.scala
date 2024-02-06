package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{Asset => ThriftAsset}
import ai.x.play.json.Jsonx
import play.api.libs.json.Format

case class Asset(assetType: AssetType,
                 version: Long,
                 id: String,
                 platform: Platform,
                 mimeType: Option[String]) {
  def asThrift = ThriftAsset.apply(AssetType.Video.asThrift, version, id, platform.asThrift, mimeType)
}

object Asset {
  implicit val assetFormat: Format[Asset] = Jsonx.formatCaseClass[Asset]
  def fromThrift(asset: ThriftAsset) = Asset(AssetType.fromThrift(asset.assetType), asset.version, asset.id, Platform.fromThrift(asset.platform), asset.mimeType)
}
