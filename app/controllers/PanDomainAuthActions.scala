package controllers

import com.gu.media.{MediaAtomMakerPermissionsProvider, Permissions}
import com.gu.media.logging.Logging
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Forbidden

trait PanDomainAuthActions extends HMACAuthActions with Logging {

  def conf: Configuration

  private def noPermissionMessage(authedUser: AuthenticatedUser): String =
    s"user ${authedUser.user.email} does not have ${Permissions.basicAccess.name} permission"

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    val isValid =
      (authedUser.user.emailDomain == "guardian.co.uk") &&
        (authedUser.multiFactor)

    val hasBasicAccess = permissionsProvider.hasPermission(
      Permissions.basicAccess,
      authedUser.user
    )

    if (!isValid) {
      log.warn(s"User ${authedUser.user.email} is not valid")
    } else if (!hasBasicAccess) {
      log.warn(noPermissionMessage(authedUser))
    }

    isValid && hasBasicAccess
  }

  override def showUnauthedMessage(message: String)(implicit
      request: RequestHeader
  ): Result =
    Forbidden(views.html.authError(message))

  override def invalidUserMessage(authedUser: AuthenticatedUser) = {
    val hasBasicAccess = permissionsProvider.hasPermission(
      Permissions.basicAccess,
      authedUser.user
    )

    if (!hasBasicAccess) noPermissionMessage(authedUser)
    else super.invalidUserMessage(authedUser)
  }

  override def authCallbackUrl: String =
    "https://" + conf.get[String]("host") + "/oauthCallback"

  override def secret: String = conf.get[String]("secret")

  def permissionsProvider: MediaAtomMakerPermissionsProvider
}
