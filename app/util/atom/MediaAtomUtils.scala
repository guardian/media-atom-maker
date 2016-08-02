package util.atom

import com.gu.atom.util._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media.Platform.{Url, Youtube}
import com.gu.contentatom.thrift.atom.media._

trait MediaAtomImplicits extends AtomImplicits[MediaAtom] {
  val dataTyper = new AtomDataTyper[MediaAtom] {
    def getData(a: Atom) = a.data.asInstanceOf[AtomData.Media].media
    def setData(a: Atom, newData: MediaAtom) =
      a.copy(data = a.data.asInstanceOf[AtomData.Media].copy(media = newData))

    def makeDefaultHtml(a: Atom): String = {
      val data = getData(a)
      val activeAssets = data.assets filter (asset => data.activeVersion.contains(asset.version))
      if (activeAssets.nonEmpty && activeAssets.forall(_.platform == Url)) {
        views.html.MediaAtom.embedUrlAssets(data).toString
      } else {
        activeAssets.headOption match {
          case Some(activeAsset) if activeAsset.platform == Youtube =>
            views.html.MediaAtom.embedYoutubeAsset(activeAsset).toString
          case _ => "<div></div>"
        }
      }
    }
  }
}

object MediaAtomImplicits extends MediaAtomImplicits
