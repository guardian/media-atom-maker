package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{AssetType => ThriftAssetType}
import play.api.libs.json._


sealed trait AssetType {
  def name: String
  def asThrift = ThriftAssetType.valueOf(name).get
}

object AssetType {
  case object Audio extends AssetType { val name = "Audio" }
  case object Video extends AssetType { val name = "Video" }

  val assetTypeReads = Reads[AssetType](json => {
    json.as[String] match {
      case "Audio" => JsSuccess(Audio)
      case "Video" => JsSuccess(Video)
    }
  })

  val assetTypeWrites = Writes[AssetType] (cat => {
    JsString(cat.name)
  })

  implicit val assetTypeFormat = Format(assetTypeReads, assetTypeWrites)

  private val types = List(Audio, Video)

  def fromThrift(p: ThriftAssetType) = types.find(_.name == p.name).get
}
