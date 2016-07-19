package data

import cats.data.Xor
import com.gu.contentatom.thrift.Atom
import javax.inject.Singleton

import com.gu.atom.data._

@Singleton
class MemoryStore extends DataStore {

  def this(initial: Map[String, Atom] = Map.empty) = {
    this()
    dataStore ++= initial
  }

  private val dataStore = collection.mutable.Map[String, Atom]()

  def getAtom(id: String) = dataStore.get(id)

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

  def listAtoms = Xor.right(dataStore.values.iterator)

}
