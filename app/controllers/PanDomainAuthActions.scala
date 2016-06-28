package controllers

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{ AWSCredentialsProviderChain, BasicAWSCredentials, _ }
import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration
import play.api.mvc._

import javax.inject.{ Inject, Singleton }

import play.api.libs.ws.WSClient

@Singleton
class PanDomainAuthActions @Inject() (
  val wsClient:WSClient, val conf: Configuration
) extends AuthActions {

  override lazy val awsCredentialsProvider: AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider(conf.getString("panda.awsCredsProfile").get)
    )

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.emailDomain == "guardian.co.uk") &&
    (authedUser.multiFactor)
  }

  override def authCallbackUrl: String = "https://" + conf.getString("host").get + "/oauthCallback"

  override lazy val domain: String = conf.getString("panda.domain").get
  override lazy val system: String = "media-atom-maker"
}
