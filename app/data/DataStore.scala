package data

import com.google.inject.ImplementedBy
import com.gu.contentatom.thrift.Atom

sealed abstract class DataStoreError(val msg: String) extends Exception(msg)

case object IDConflictError extends DataStoreError("Atom ID already exists")

@ImplementedBy(classOf[MemoryStore])
trait DataStore {
  def getMediaAtom(id: String): Option[Atom]

  def createMediaAtom(atom: Atom): Unit

  /* this will only allow the update if the version in atom is later
   * than the version stored in the database, otherwise it will report
   * it as a version conflict error */
  def updateMediaAtom(atom: Atom, currentVersion: Long): Unit
}
