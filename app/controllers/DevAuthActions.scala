package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorRef
import akka.event.Logging.StandardOutLogger
import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model._
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader

@Singleton
class DevAuthActions @Inject()(val wsClient: WSClient) extends AuthActions {

  override val system = "media-atom-maker"
  override val domain = "localhost"
  override val authCallbackUrl = ""

  override def validateUser(authedUser: AuthenticatedUser): Boolean = true

  override def extractAuth(request: RequestHeader): AuthenticationStatus = {
    val emptyUser = User(firstName = "", lastName = "", email = "", avatarUrl = None)
    val never = -1
    Authenticated(
      AuthenticatedUser(
        user = emptyUser,
        authenticatingSystem = system,
        authenticatedIn = Set(system),
        expires = never,
        multiFactor = false
      )
    )
  }

  override lazy val domainSettingsRefreshActor: ActorRef = new StandardOutLogger() {
    override def !(message: Any)(implicit sender: ActorRef): Unit = {
      message match {
        case com.gu.pandomainauth.Refresh => // do nothing
        case _ => super.!(message)
      }
    }
  }

  override def settings: PanDomainAuthSettings = {
    val emptySettings = Map(
      "googleAuthClientId" -> "",
      "googleAuthSecret" -> "",
      "secret" -> "",
      "publicKey" -> "",
      "privateKey" -> ""
    )
    PanDomainAuthSettings(emptySettings)
  }
}
