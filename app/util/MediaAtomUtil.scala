package util

import com.gu.contentatom.thrift.atom.media.{ Asset, MediaAtom }


object MediaAtomUtil {

  def latestAsset(mediaAtom: MediaAtom): Option[Asset] = {
    val activeVersion = mediaAtom.activeVersion
    mediaAtom.assets.find(_.version == activeVersion)
  }

}
