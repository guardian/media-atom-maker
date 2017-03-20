package integration

import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import integration.services.{Config, GuHttp, TestAtomJsonGenerator}
import play.api.Logger

import scala.collection.mutable.ListBuffer

class IntegrationTestBase extends FunSuite with Matchers with Eventually with IntegrationPatience with GuHttp with TestAtomJsonGenerator with BeforeAndAfterAll {

  val targetBaseUrl: String = Config.targetBaseUrl

  def apiUri(atomId: String): String = s"$targetBaseUrl/api/atom/$atomId"

  var atomStore = new ListBuffer[String]() /* Add all created atoms IDs to this list as first action after atom created. This allows for test cleanup outside the test flow  */

  def deleteAtom(id: String) = {
    Logger.info(s"Deleting atom $id")
    gutoolsDelete(s"$targetBaseUrl/api2/atom/$id")
  }

  override def afterAll(): Unit = {
    atomStore.foreach{ e => deleteAtom(e) }
    super.afterAll()
  }

  def addAtomToStore(atomId: String): ListBuffer[String] = {
    Logger.info(s"Adding $atomId to Atom Store")
    atomStore += atomId
  }

}


