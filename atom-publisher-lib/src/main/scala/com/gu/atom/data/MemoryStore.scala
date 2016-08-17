package data

import cats.data.Xor
import com.gu.contentatom.thrift.Atom

import com.gu.atom.data._

class MemoryStore extends DataStore {

  def this(initial: Map[String, Atom] = Map.empty) = {
    this()
    dataStore ++= initial
  }

  private val dataStore = collection.mutable.Map[String, Atom]()

  def getAtom(id: String) = dataStore.get(id)

  def getPublishedAtom(id: String) = dataStore.get(id)

  def createAtom(atom: Atom) = dataStore.synchronized {
    if(dataStore.get(atom.id).isDefined) {
      fail(IDConflictError)
    } else {
      succeed(dataStore(atom.id) = atom)
    }
  }

  def updateAtom(newAtom: Atom) = dataStore.synchronized {
    getAtom(newAtom.id) match {
      case Some(oldAtom) =>
        if(oldAtom.contentChangeDetails.revision >=
             newAtom.contentChangeDetails.revision) {
          fail(VersionConflictError(newAtom.contentChangeDetails.revision))
        } else {
          succeed(dataStore(newAtom.id) = newAtom)
        }
      case None => fail(IDNotFound)
    }
  }

  def updatePublishedAtom(newAtom: Atom) = dataStore.synchronized {
    getPublishedAtom(newAtom.id) match {
      case Some(oldAtom) =>
        if(oldAtom.contentChangeDetails.revision >=
          newAtom.contentChangeDetails.revision) {
          fail(VersionConflictError(newAtom.contentChangeDetails.revision))
        } else {
          succeed(dataStore(newAtom.id) = newAtom)
        }
      case None => fail(IDNotFound)
    }
  }

  def listAtoms = Xor.right(dataStore.values.iterator)

  def listPublishedAtoms = Xor.right(dataStore.values.iterator)
}
