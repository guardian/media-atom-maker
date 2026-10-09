package controllers

import com.gu.hmac.{HMACInvalidTokenError, HMACSignatory, HMACToken}
import com.gu.media.{MediaAtomMakerPermissionsProvider, Permissions}
import com.gu.media.logging.Logging
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Configuration
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Forbidden

import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.time.{Duration, Instant, ZoneOffset}
import java.time.format.{
  DateTimeFormatter,
  DateTimeParseException,
  ResolverStyle
}
import java.util.{Base64, Locale}
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

trait PanDomainAuthActions extends HMACAuthActions with Logging {

  def conf: Configuration

  private val httpDateFormatter = DateTimeFormatter
    .ofPattern("EEE, dd MMM uuuu HH:mm:ss 'GMT'", Locale.US)
    .withZone(ZoneOffset.UTC)
    .withResolverStyle(ResolverStyle.STRICT)

  override def validateHMACHeadersWithSecret(
      secretKey: String,
      dateHeader: String,
      authorizationHeader: String,
      uri: URI
  ): Boolean = try {
    // Accept legacy English "Sept" for freshness checks only, not for signing.
    val date = Instant.from(
      httpDateFormatter.parse(dateHeader.replace(" Sept ", " Sep "))
    )
    val token = HMACToken.get(authorizationHeader).value
    val age = Duration.between(date, clock.instant()).abs()

    age.compareTo(Duration.ofMinutes(HmacValidDurationInMinutes)) <= 0 && {
      // The dependency re-formats the date using the JVM locale before signing.
      // Verify the exact header instead, so neither locale nor the alias alters it.
      val mac = Mac.getInstance(HMACSignatory.Algorithm)
      mac.init(
        new SecretKeySpec(secretKey.getBytes(UTF_8), HMACSignatory.Algorithm)
      )
      val expected = Base64.getEncoder.encode(
        mac.doFinal(s"$dateHeader\n${uri.getPath}".getBytes(UTF_8))
      )
      MessageDigest.isEqual(expected, token.getBytes(UTF_8))
    }
  } catch {
    case _: DateTimeParseException =>
      log.warn("Rejecting HMAC authentication: invalid HTTP date")
      false
    case _: HMACInvalidTokenError =>
      log.warn("Rejecting HMAC authentication: malformed token header")
      false
  }

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
