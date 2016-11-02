package test

import com.gu.atom.play.test.AtomSuite

import java.util.Date
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.{ AuthenticatedUser, User }
import play.api.test.FakeRequest

import play.api.inject.{ bind, Binding }

trait AuthTests extends AtomSuite {

  override def customOverrides: Seq[Binding[_]] =
    super.customOverrides :+ (bind(classOf[HMACAuthActions]).to(classOf[TestPandaAuth]))

  val authExpiary = 3600000L

  def getAuthActions(implicit conf: AtomTestConf): HMACAuthActions = conf.iget[HMACAuthActions]

  def testUser: AuthenticatedUser = AuthenticatedUser(
    user = User("Homer", "Simpson", "homer.simpson@guardian.co.uk", None),
    authenticatingSystem = "test",
    authenticatedIn = Set("test"),
    expires = new Date().getTime + authExpiary,
    multiFactor = true
  )

  def requestWithCookies(implicit conf: AtomTestConf) =
    FakeRequest().withCookies(getAuthActions.generateCookies(testUser): _*)

}
