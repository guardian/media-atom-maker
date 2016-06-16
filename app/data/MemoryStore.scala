package data

import com.gu.contentatom.thrift.Atom
import javax.inject.Singleton

import util.atom.MediaAtomImplicits

@Singleton
class MemoryStore extends DataStore
    with MediaAtomImplicits {

  def this(initial: Map[String, Atom] = Map.empty) = {
    this()
    dataStore ++= initial
  }

  private val dataStore = collection.mutable.Map[String, Atom]()

  def getMediaAtom(id: String) = dataStore.get(id)

  def createMediaAtom(atom: Atom) = dataStore.synchronized {
    if(dataStore.get(atom.id).isDefined)
      throw IDConflictError
    else
      dataStore(atom.id) = atom
  }

  def updateMediaAtom(newAtom: Atom) = dataStore.synchronized {
    getMediaAtom(newAtom.id) match {
      case Some(oldAtom) =>
        if(oldAtom.contentChangeDetails.revision >= newAtom.contentChangeDetails.revision)
          throw new VersionConflictError(newAtom.tdata.activeVersion)
        dataStore(newAtom.id) = newAtom
      case None => throw IDNotFound
    }
  }

  def listAtoms = dataStore.values

}
