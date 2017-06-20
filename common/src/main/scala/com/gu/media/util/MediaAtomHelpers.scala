package com.gu.media.util

import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, MediaAtom, Platform}

object MediaAtomHelpers {
  def updateAtom(atom: Atom)(fn: MediaAtom => MediaAtom): Atom = {
    val before = atom.data.asInstanceOf[AtomData.Media].media
    val after = fn(before)

    atom.copy(data = AtomData.Media(after))
  }

  def addAsset(mediaAtom: MediaAtom, asset: VideoAsset): MediaAtom = {
    val version = getNextVersion(mediaAtom.assets)
    val assets = getAssets(asset, version)

    mediaAtom.copy(assets = assets ++ mediaAtom.assets)
  }

  private def getAssets(asset: VideoAsset, version: Long): List[Asset] = asset match {
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
}
