package com.gu.media.util

import com.gu.contentatom.thrift.{Atom, AtomData, ChangeRecord, User}
import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, Platform, MediaAtom => ThriftMediaAtom}
import com.gu.media.model._
import org.joda.time.DateTime

object MediaAtomHelpers {
  def updateAtom(atom: Atom, user: User)(fn: ThriftMediaAtom => ThriftMediaAtom): Atom = {
    val before = atom.data.asInstanceOf[AtomData.Media].media
    val after = fn(before)

    atom.copy(
      data = AtomData.Media(after),
      contentChangeDetails = atom.contentChangeDetails.copy(
        lastModified = Some(ChangeRecord(DateTime.now().getMillis, Some(user))),
        revision = atom.contentChangeDetails.revision + 1
      )
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

    // remove any existing assets that have the same version
    val atomAssets = mediaAtom.assets.filter( a => a.version != version)
    val updatedAssets = assets ++ atomAssets

    mediaAtom.copy(assets = updatedAssets)
  }

  def getUser(email: String): User = {
    email match {
      case s"$firstname.$lastname.$_@$_" =>
        User(email, Some(firstname.capitalize), Some(lastname.capitalize))
      case s"$firstname.$lastname@$_" =>
        User(email, Some(firstname.capitalize), Some(lastname.capitalize))
      case _ =>
        User(email)
    }
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
