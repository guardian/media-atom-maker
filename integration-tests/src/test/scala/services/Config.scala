package integration.services

import com.typesafe.config.ConfigFactory

import scala.util.Try

object Config {

  val config = ConfigFactory.load()

  val stage: String = Try(sys.env("INT_TEST_TARGET")).getOrElse("CODE")

  /* Test */
  lazy val targetBaseUrl = if (stage == "PROD") config.getString("targetBaseUrlProd") else config.getString("targetBaseUrl")
  lazy val asset = config.getString("asset")
  lazy val assetId = config.getString("assetId")
  lazy val channelId = config.getString("channelId")
  lazy val youtubeCategoryId = config.getString("youtubeCategoryId")

  /* Panda */
  lazy val domain = if (stage == "PROD") config.getString("domain-prod") else config.getString("domain")
  lazy val system = config.getString("system")
  lazy val userFirstName = config.getString("userFirstName")
  lazy val userSecondName = config.getString("userSecondName")
  lazy val userEmail = config.getString("userEmail")
  lazy val credentialsProvider = config.getString("credentialsProvider")
}
