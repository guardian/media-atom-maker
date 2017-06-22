package com.gu.media.util

import com.gu.contentatom.thrift.{Atom, AtomData}
import com.gu.contentatom.thrift.atom.media.{Asset, AssetType, MediaAtom, Platform}

object MediaAtomHelpers {
  def updateAtom(atom: Atom)(fn: MediaAtom => MediaAtom): Atom = {
    val before = atom.data.asInstanceOf[AtomData.Media].media
    val after = fn(before)

    atom.copy(
      data = AtomData.Media(after),
      contentChangeDetails = atom.contentChangeDetails.copy(revision = atom.contentChangeDetails.revision + 1)
    )
  }

  def addAsset(mediaAtom: MediaAtom, id: String): MediaAtom = {
    val version = getNextVersion(mediaAtom.assets)
    val asset = Asset(AssetType.Video, version, id, Platform.Youtube, mimeType = None)

    mediaAtom.copy(assets = asset +: mediaAtom.assets)
  }

  private def getNextVersion(assets: Seq[Asset]): Long = {
    if(assets.isEmpty) {
      1
    } else {
      assets.map(_.version).max + 1
    }
  }
}
