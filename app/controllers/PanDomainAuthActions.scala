package controllers

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth._
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration
import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient


class PanDomainAuthActions(val wsClient:WSClient, val conf: Configuration, applicationLifeCycle: ApplicationLifecycle)
  extends HMACAuthActions {

  applicationLifeCycle.addStopHook {
    () => Future {
      shutdown
      wsClient.close()
    }
  }

  override lazy val awsCredentialsProvider: AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider(conf.getString("panda.awsCredsProfile").getOrElse("panda")),
      new InstanceProfileCredentialsProvider(false)
    )

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.emailDomain == "guardian.co.uk") &&
    (authedUser.multiFactor)
  }

  override def authCallbackUrl: String = "https://" + conf.getString("host").get + "/oauthCallback"

  override lazy val domain: String = conf.getString("panda.domain").get
  override lazy val system: String = "video"

  override def secret: String = conf.getString("secret").get
}
