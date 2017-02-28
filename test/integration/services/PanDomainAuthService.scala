package integration.services

import java.util.Date

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth._
import com.gu.pandomainauth.{PanDomainAuth, PublicSettings}
import com.gu.pandomainauth.model.{AuthenticatedUser, User}
import com.gu.pandomainauth.service.CookieUtils

class PanDomainAuthService (val system: String, val domain: String) extends PanDomainAuth {

  override lazy val awsCredentialsProvider: AWSCredentialsProvider =
    new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider(Config.credentialsProvider),
      new InstanceProfileCredentialsProvider(false)
    )

  val thirtyMinsMillis: Long = 1000 * 60 * 30

  def authedUser(email: String, firstName: String, lastName: String, domain: String, authenticatingSystem: String, authenticatedIn: Set[String] = Set()): AuthenticatedUser = {
    AuthenticatedUser(
      user = User(firstName, lastName, email, None),
      authenticatingSystem = authenticatingSystem,
      authenticatedIn = authenticatedIn + authenticatingSystem,
      expires = new Date().getTime + thirtyMinsMillis,
      multiFactor = true
    )
  }

  def generateAsymCookie(user: AuthenticatedUser) = CookieUtils.generateCookieData(user, settings.privateKey)

}

case class PandaCookie(key: String, value: String)
object PandaCookie {
  def apply(
             domain: String = Config.domain,
             system: String = Config.system,
             additionalAuthedSystems: Set[String] = Set(), /* This will make it easier to add additional systems that we need to auth against for downstream test assertions */
             firstName:String = Config.userFirstName,
             lastName:String  = Config.userSecondName,
             email:String     = Config.userEmail
           ): PandaCookie = {
    val authService = new PanDomainAuthService(system, domain)

    try {
      val user = authService.authedUser(email, firstName, lastName, domain, system, additionalAuthedSystems)
      val asymCookieValue = authService.generateAsymCookie(user)
      PandaCookie(PublicSettings.assymCookieName, asymCookieValue)
    } finally {
      authService.shutdown
    }
  }
}
