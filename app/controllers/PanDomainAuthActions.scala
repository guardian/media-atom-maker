package controllers

import com.gu.media.{MediaAtomMakerPermissionsProvider, Permissions}
import com.gu.media.logging.Logging
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration

trait PanDomainAuthActions extends HMACAuthActions with Logging {

  def conf: Configuration

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    val isValid =
      (authedUser.user.emailDomain == "guardian.co.uk") &&
      (authedUser.multiFactor)

    val hasBasicAccessExplicitly = permissionsProvider.hasPermission(Permissions.basicAccess, authedUser.user)
    val hasAnyAtomMakerPermission = permissionsProvider.hasAnyAtomMakerPermission(authedUser.user)

    if(!isValid) {
      log.warn(s"User ${authedUser.user.email} is not valid")
    }
    else if (!hasAnyAtomMakerPermission) {
      log.warn(s"User ${authedUser.user.email} does not have any MAM permissions")
    }
    else if (!hasBasicAccessExplicitly) {
      log.warn(s"User ${authedUser.user.email} does not have media_atom_maker_access permission but does have other MAM permissions")
    }

    isValid
  }

  override def authCallbackUrl: String = "https://" + conf.get[String]("host") + "/oauthCallback"

  override def secret: String = conf.get[String]("secret")

  def permissionsProvider: MediaAtomMakerPermissionsProvider
}
