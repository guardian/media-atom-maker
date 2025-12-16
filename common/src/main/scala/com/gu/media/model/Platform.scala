package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{Platform => ThriftPlatform}
import play.api.libs.json._

sealed trait Platform {
  def name: String
  def asThrift = ThriftPlatform.valueOf(name).get
}

object Platform {
  case object Youtube extends Platform { val name = "Youtube" }
  case object Facebook extends Platform { val name = "Facebook" }
  case object Dailymotion extends Platform { val name = "Dailymotion" }
  case object Mainstream extends Platform { val name = "Mainstream" }
  case object Url extends Platform { val name = "Url" }

  val platformReads = Reads[Platform](json => {
    json.as[String].toLowerCase match {
      case "youtube"     => JsSuccess(Youtube)
      case "facebook"    => JsSuccess(Facebook)
      case "dailymotion" => JsSuccess(Dailymotion)
      case "mainstream"  => JsSuccess(Mainstream)
      case "url"         => JsSuccess(Url)
    }
  })

  val platformWrites: Writes[Platform] = Writes[Platform](cat => {
    JsString(cat.name)
  })

  implicit val platformFormat: Format[Platform] =
    Format(platformReads, platformWrites)

  private val types = List(Youtube, Facebook, Dailymotion, Mainstream, Url)

  def fromThrift(p: ThriftPlatform) = types.find(_.name == p.name).get

  /**
   * The atom-level platform field tells us authoritatively if atom is for Youtube or Self-hosted video.
   *
   * To derive an atom-level platform field from models with potentially missing data:
   * - use atom level platform if provided
   * - otherwise use the platform field of the active asset
   * - otherwise use the platform field of the first asset, if there are any assets
   * - otherwise default to Youtube
   * @param atomPlatform
   * @param activeAssetPlatform
   * @param firstAssetPlatform
   * @return
   */
  def getAtomPlatform(atomPlatform: Option[Platform], activeAssetPlatform: Option[Platform], firstAssetPlatform: Option[Platform]): Platform =
    atomPlatform
      .orElse(activeAssetPlatform)
      .orElse(firstAssetPlatform)
      .getOrElse(Youtube)

}
