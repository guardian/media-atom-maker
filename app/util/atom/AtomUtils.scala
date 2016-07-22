package util.atom

import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media.Platform.{Url, Youtube}
import com.gu.contentatom.thrift.atom.media._

trait AtomDataTyper[D] {
  def getData(a: Atom): D
  def setData(a: Atom, newData: D): Atom
  def makeDefaultHtml(a: Atom): String
}

trait AtomImplicits[D] {
  val dataTyper: AtomDataTyper[D]
  implicit class AtomWithData(atom: Atom) {
    def tdata = dataTyper.getData(atom)
    def withData(data: D): Atom =
      dataTyper.setData(atom, data).updateDefaultHtml
    def updateData(f: D => D): Atom = withData(f(atom.tdata))
    def withRevision(f: Long => Long): Atom = atom.copy(
      contentChangeDetails = atom.contentChangeDetails.copy(
        revision = f(atom.contentChangeDetails.revision)
      )
    )
    def withRevision(newRevision: Long): Atom = withRevision(_ => newRevision)
    def updateDefaultHtml = atom.copy(defaultHtml = dataTyper.makeDefaultHtml(atom))
  }
}

trait MediaAtomImplicits extends AtomImplicits[MediaAtom] {
  val dataTyper = new AtomDataTyper[MediaAtom] {
    def getData(a: Atom) = a.data.asInstanceOf[AtomData.Media].media
    def setData(a: Atom, newData: MediaAtom) =
      a.copy(data = a.data.asInstanceOf[AtomData.Media].copy(media = newData))

    def makeDefaultHtml(a: Atom): String = {
      val data = getData(a)
      val activeAssets = data.assets filter (asset => data.activeVersion.contains(asset.version))
      if (activeAssets.nonEmpty && activeAssets.forall(_.platform == Url)) {
        views.html.MediaAtom.embedUrlAssets(activeAssets).toString
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
