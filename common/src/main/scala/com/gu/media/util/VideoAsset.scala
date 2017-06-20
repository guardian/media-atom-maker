package com.gu.media.util

import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, MediaAtom, Platform}
import com.gu.media.youtube.YouTubeLink
import play.api.libs.json._

case class VideoSource(src: String, mimeType: String)

sealed abstract class VideoAsset
case class YouTubeAsset(id: String) extends VideoAsset
case class SelfHostedAsset(sources: List[VideoSource]) extends VideoAsset

object VideoAsset {
  implicit val reads: Reads[VideoAsset] = Reads { v => apply(v) }

  def apply(json: JsValue): JsResult[VideoAsset] = {
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

  def addToAtom(atom: MediaAtom, asset: VideoAsset): MediaAtom = {
    val version = getNextVersion(atom.assets)
    val assets = getThriftAssets(asset, version)

    atom.copy(assets = assets ++ atom.assets)
  }

  private def getThriftAssets(asset: VideoAsset, version: Long): List[Asset] = asset match {
    case YouTubeAsset(id) =>
      val asset = Asset(AssetType.Video, version, id, Platform.Youtube, mimeType = None)

      List(asset)

    case SelfHostedAsset(sources) =>
      val assets = sources.map { case VideoSource(src, mimeType) =>
        Asset(AssetType.Video, version, src, Platform.Url, Some(mimeType))
      }

      assets
  }

  private def getNextVersion(assets: Seq[Asset]): Long = {
    if(assets.isEmpty) {
      1
    } else {
      assets.map(_.version).max + 1
    }
  }

  private def parseAssetsByPlatform(platform: String, rawAssets: List[JsValue]): JsResult[VideoAsset] = {
    (platform, rawAssets) match {
      case (p, assets) if p == "Youtube" =>
        assets match {
          case asset :: Nil =>
            parseYouTubeId(asset)

          case _ =>
            JsError(s"Expected one YouTube asset, got ${assets.size}")
        }

      case (p, assets) if p == "Url" =>
        // TODO MRB: is there already a function to do this (equivalent of Future.sequence)?
        val results = assets.map(parseSelfHostedAsset)
        val failures = results.collect { case JsError(err) => err }

        if(failures.nonEmpty) {
          JsError(failures.flatten)
        } else {
          JsSuccess(SelfHostedAsset(results.collect { case JsSuccess(asset, _) => asset }))
        }

      case _ =>
        JsError(s"Unknown platform $platform")
    }
  }

  private def parseYouTubeId(rawAsset: JsValue): JsResult[VideoAsset] = {
    (rawAsset \ "id").validate[String].flatMap {
      case id if id.startsWith("http") =>
        JsError(s"Expected YouTube ID, got $id")

      case id =>
        JsSuccess(YouTubeAsset(id))
    }
  }

  private def parseYouTubeUri(rawAsset: JsValue): JsResult[VideoAsset] = {
    (rawAsset \ "uri").validate[String].flatMap {
      case YouTubeLink(id) =>
        JsSuccess(YouTubeAsset(id))

      case other =>
        JsError(s"Expected YouTube link, got $other")
    }
  }

  private def parseSelfHostedAsset(rawAsset: JsValue): JsResult[VideoSource] = for {
    id <- (rawAsset \ "uri").validate[String]
    mimeType <- (rawAsset \ "mimeType").validate[String]
  } yield {
    VideoSource(id, mimeType)
  }
}
