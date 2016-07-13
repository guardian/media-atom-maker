package test

// this is a test version of the API which overrides the panda
// settings to use it's own test key (which is not used for anything
// else and so does not need to be kept secure

import akka.actor.{ Actor, Props }
import controllers.PanDomainAuthActions

import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient

import javax.inject.Inject

import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.libs.ws.WSClient

import javax.inject.Inject

class DummyActor extends Actor {
  def receive = {
    case com.gu.pandomainauth.Refresh =>
  }
}

class TestPandaAuth @Inject() (
  wsClient: WSClient, conf: Configuration,
  applicationLifeCycle: ApplicationLifecycle)
    extends PanDomainAuthActions (wsClient, conf, applicationLifeCycle) {

  override def authCallbackUrl = ""
  override def validateUser(user: AuthenticatedUser) = true

  override lazy val domainSettingsRefreshActor =
    actorSystem.actorOf(Props[DummyActor])
  override lazy val settingsMap = {
    Map(
      "googleAuthClientId" -> "test",
      "googleAuthSecret"   -> "test",
      "secret"             -> "test",
      "publicKey"          -> TestKeys.publicKey,
      "privateKey"         -> TestKeys.privateKey
    )
  }

  override lazy val domain = "test"
  override lazy val system = "test"
}
