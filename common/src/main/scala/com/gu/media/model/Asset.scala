package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{Asset => ThriftAsset}
import com.gu.ai.x.play.json.Encoders._
import com.gu.ai.x.play.json.Jsonx
import play.api.libs.json.OFormat

case class Asset(
    assetType: AssetType,
    version: Long,
    id: String,
    platform: Platform,
    mimeType: Option[String]
) {
  def asThrift = ThriftAsset.apply(
    assetType.asThrift,
    version,
    id,
    platform.asThrift,
    mimeType
  )
}

object Asset {
  implicit val assetFormat: OFormat[Asset] = Jsonx.formatCaseClass[Asset]
  def fromThrift(asset: ThriftAsset) = Asset(
    AssetType.fromThrift(asset.assetType),
    asset.version,
    asset.id,
    Platform.fromThrift(asset.platform),
    asset.mimeType
  )
}
