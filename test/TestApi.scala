package test

// this is a test version of the API which overrides the panda
// settings to use it's own test key (which is not used for anything
// else and so does not need to be kept secure

import com.gu.pandomainauth.PanDomainAuth
import data._

import play.api.Configuration
import play.api.libs.ws.WSClient

import javax.inject.Inject

import com.gu.pandomainauth.model.PanDomainAuthSettings

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.actor.{Props, Actor, ActorSystem}
import akka.agent.Agent

import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.libs.ws.WSClient

import javax.inject.Inject

class DummyActor extends Actor {
  def receive = {
    case com.gu.pandomainauth.Refresh =>
  }
}

class TestPandaAuth @Inject() (val wsClient: WSClient) extends AuthActions {

  def authCallbackUrl = ""
  def validateUser(user: AuthenticatedUser) = true

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

  val domain = "test"
  def system = "test"
}

// class TestApiController @Inject() (dataStore: DataStore,
//                          publisher: AtomPublisher,
//                          conf: Configuration,
//                          wsClient: WSClient)
//     extends controllers.Api(dataStore, publisher, conf, wsClient)
//     with TestPandaAuth
