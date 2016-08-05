package com.gu.atom.util

import com.gu.contentatom.thrift._

trait AtomDataTyper[D] {
  def getData(a: Atom): D
  def setData(a: Atom, newData: D): Atom
  def makeDefaultHtml(a: Atom): String
}

object AtomDataTyper {
  val general = new AtomDataTyper[AtomData] {
    def getData(a: Atom) = a.data
    def setData(a: Atom, newData: AtomData) =
      a.copy(data = newData)
    def makeDefaultHtml(a: Atom) = a.defaultHtml
  }
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
    def bumpRevision = withRevision(_ + 1)
    def updateDefaultHtml = atom.copy(defaultHtml = dataTyper.makeDefaultHtml(atom))
  }
}

// default implementation that doesn't really do anything with the atom data
trait AtomImplicitsGeneral extends AtomImplicits[AtomData] {
  val dataTyper = AtomDataTyper.general
}

object AtomImplicitsGeneral extends AtomImplicitsGeneral
