package model

import com.gu.contentatom.thrift.atom.media.{AssetType => ThriftAssetType}
import play.api.libs.json._


sealed trait AssetType {
  def name: String
  def asThrift = ThriftAssetType.valueOf(name).get
}

object AssetType {
  case object AUDIO extends AssetType { val name = "AUDIO" }
  case object VIDEO extends AssetType { val name = "VIDEO" }

  val assetTypeReads = Reads[AssetType](json => {
    json.as[String] match {
      case "AUDIO" => JsSuccess(AUDIO)
      case "VIDEO" => JsSuccess(VIDEO)
    }
  })

  val assetTypeWrites = Writes[AssetType] (cat => {
    JsString(cat.name)
  })

  implicit val assetTypeFormat = Format(assetTypeReads, assetTypeWrites)

  private val types = List(AUDIO, VIDEO)

  def fromThrift(p: ThriftAssetType) = types.find(_.name == p.name).get
}