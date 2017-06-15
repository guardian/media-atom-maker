package util

import model.Platform.Youtube.{name => YouTubePlatform}
import model.Platform.Url.{name => UrlPlatform}
import model.{Asset, AssetType, Platform}
import play.api.libs.json._

case class AddAssetRequest(assets: List[Asset])

object AddAssetRequest {
  private val emptyAsset = Asset(
    assetType = AssetType.Video,
    version = -1, // filled in later,
    id = "",
    platform = Platform.Url,
    mimeType = None
  )

  implicit val reads: Reads[AddAssetRequest] = Reads { v => apply(v) }.map(AddAssetRequest(_))

  def apply(json: JsValue): JsResult[List[Asset]] = {
    val ret = for {
      platform <- (json \ "platform").validate[String]
      rawAssets <- (json \ "assets").validate[List[JsValue]]

      assets <- parseAssetsByPlatform(platform, rawAssets)
    } yield {
      assets
    }

    // try at the top level for legacy compat
    ret orElse parseYouTubeUri(json)
  }

  private def parseAssetsByPlatform(platform: String, rawAssets: List[JsValue]): JsResult[List[Asset]] = {
    (platform, rawAssets) match {
      case (p, assets) if p == YouTubePlatform =>
        assets match {
          case asset :: Nil =>
            parseYouTubeId(asset)

          case _ =>
            JsError(s"Expected one YouTube asset, got ${assets.size}")
        }

      case (p, assets) if p == UrlPlatform =>
        // TODO MRB: is there already a function to do this (equivalent of Future.sequence)?
        val results = assets.map(parseSelfHostedAsset)
        val failures = results.collect { case JsError(err) => err }

        if(failures.nonEmpty) {
          JsError(failures.flatten)
        } else {
          JsSuccess(results.collect { case JsSuccess(asset, _) => asset })
        }

      case _ =>
        JsError(s"Unknown platform $platform")
    }
  }

  private def parseYouTubeId(rawAsset: JsValue): JsResult[List[Asset]] = {
    (rawAsset \ "id").validate[String].flatMap {
      case id if id.startsWith("http") =>
        JsError(s"Expected YouTube ID, got $id")

      case id =>
        JsSuccess(List(emptyAsset.copy(id = id, platform = Platform.Youtube)))
    }
  }

  private def parseYouTubeUri(rawAsset: JsValue): JsResult[List[Asset]] = {
    (rawAsset \ "uri").validate[String].flatMap {
      case ThriftUtil.youtube(id) =>
        JsSuccess(List(emptyAsset.copy(id = id, platform = Platform.Youtube)))

      case other =>
        JsError(s"Expected YouTube link, got $other")
    }
  }

  private def parseSelfHostedAsset(rawAsset: JsValue): JsResult[Asset] = for {
    id <- (rawAsset \ "uri").validate[String]
    mimeType <- (rawAsset \ "mimeType").validate[String]
  } yield emptyAsset.copy(
    id = id,
    platform = Platform.Url,
    mimeType = Some(mimeType)
  )
}
