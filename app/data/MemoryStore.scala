package data

import com.gu.contentatom.thrift.Atom
import javax.inject.Singleton

@Singleton
class MemoryStore extends DataStore {

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

  def updateMediaAtom(atom: Atom) = dataStore.synchronized {
    throw VersionConflictError
  }
}
