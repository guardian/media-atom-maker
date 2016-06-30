package data

import cats.data.Xor
import com.google.inject.ImplementedBy
import com.gu.contentatom.thrift.Atom

sealed abstract class DataStoreError(val msg: String) extends Exception(msg)

case object IDConflictError extends DataStoreError("Atom ID already exists")
case object IDNotFound extends DataStoreError("Atom ID not in datastore")
case object ReadError extends DataStoreError("Read error")

case class  DataError(info: String) extends DataStoreError(info)
case class  VersionConflictError(requestVer: Long)
    extends DataStoreError(s"Update has version $requestVer, which is earlier or equal to data store version")

//@ImplementedBy(classOf[MemoryStore])
@ImplementedBy(classOf[DynamoDataStore])
trait DataStore {

  type DataStoreResult[R] = Xor[DataStoreError, R]

  def fail(error: DataStoreError): DataStoreResult[Nothing] = Xor.left(error)
  def succeed[R](result: => R): DataStoreResult[R] = Xor.right(result)

  def getMediaAtom(id: String): Option[Atom]

  def createMediaAtom(atom: Atom): DataStoreResult[Unit]

  /* this will only allow the update if the version in atom is later
   * than the version stored in the database, otherwise it will report
   * it as a version conflict error */
  def updateMediaAtom(newAtom: Atom): DataStoreResult[Unit]

  def listAtoms: DataStoreResult[TraversableOnce[Atom]]

}
