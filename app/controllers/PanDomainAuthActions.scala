package controllers

import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration

trait PanDomainAuthActions extends HMACAuthActions {

  def conf: Configuration

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.emailDomain == "guardian.co.uk") &&
    (authedUser.multiFactor)
  }

  override def authCallbackUrl: String = "https://" + conf.get[String]("host") + "/oauthCallback"

  override def secret: String = conf.get[String]("secret")
}
