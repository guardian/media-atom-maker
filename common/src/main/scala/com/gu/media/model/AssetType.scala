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
  case object Subtitles extends AssetType { val name = "Subtitles" }

  val assetTypeReads = Reads[AssetType](json => {
    json.as[String] match {
      case "Audio" => JsSuccess(Audio)
      case "Video" => JsSuccess(Video)
      case "Subtitles" => JsSuccess(Subtitles)
    }
  })

  val assetTypeWrites: Writes[AssetType] = Writes[AssetType] (cat => {
    JsString(cat.name)
  })

  implicit val assetTypeFormat: Format[AssetType] = Format(assetTypeReads, assetTypeWrites)

  private val types = List(Audio, Video, Subtitles)

  def fromThrift(p: ThriftAssetType) = types.find(_.name == p.name).get
}
