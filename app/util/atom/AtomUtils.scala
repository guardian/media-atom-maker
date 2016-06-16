package util.atom

import com.gu.contentatom.thrift._
import atom.media._

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
    def makeDefaultHtml(a: Atom) = {
      val data = getData(a)
      data.assets
        .find(_.version == data.activeVersion)
        .map(asset => views.html.MediaAtom.embedAsset(asset).toString)
        .getOrElse(s"<div></div>")
    }
  }
}

object MediaAtomImplicits extends MediaAtomImplicits
