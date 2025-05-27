package com.gu.media.util

import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, Platform, MediaAtom => ThriftMediaAtom}
import com.gu.media.model._

object MediaAtomHelpers {
  def updateAtom(atom: Atom)(fn: ThriftMediaAtom => ThriftMediaAtom): Atom = {
    val before = atom.data.asInstanceOf[AtomData.Media].media
    val after = fn(before)

    atom.copy(
      data = AtomData.Media(after),
      contentChangeDetails = atom.contentChangeDetails.copy(revision = atom.contentChangeDetails.revision + 1)
    )
  }

  def getCurrentAssetVersion(mediaAtom: MediaAtom): Option[Long] = {
    if (mediaAtom.assets.isEmpty) {
      None
    } else {
      Some(mediaAtom.assets.map(_.version).max)
    }
  }

  def getCurrentAssetVersion(mediaAtom: ThriftMediaAtom): Option[Long] = {
    if (mediaAtom.assets.isEmpty) {
      None
    } else {
      Some(mediaAtom.assets.map(_.version).max)
    }
  }

  def getNextAssetVersion(mediaAtom: ThriftMediaAtom): Long = {
    getCurrentAssetVersion(mediaAtom).getOrElse(0L) + 1
  }

  def addAsset(mediaAtom: ThriftMediaAtom, asset: VideoAsset, version: Long): ThriftMediaAtom = {
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
}
