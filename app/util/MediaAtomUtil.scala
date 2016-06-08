package util

import com.gu.contentatom.thrift.atom.media.{ Asset, MediaAtom }


object MediaAtomUtil {

  def latestAsset(mediaAtom: MediaAtom): Option[Asset] = mediaAtom.assets.sortBy(_.version).reverse.headOption

}
