package integration.services

import com.typesafe.config.ConfigFactory

object Config {

  val config = ConfigFactory.load()

  /* Test */
  lazy val targetBaseUrl = config.getString("targetBaseUrl")
  lazy val asset = config.getString("asset")
  lazy val assetId = config.getString("assetId")
  lazy val channelId = config.getString("channelId")
  lazy val youtubeCategoryId = config.getString("youtubeCategoryId")

  /* Panda */
  lazy val domain = config.getString("domain")
  lazy val system = config.getString("system")
  lazy val userFirstName = config.getString("userFirstName")
  lazy val userSecondName = config.getString("userSecondName")
  lazy val userEmail = config.getString("userEmail")
  lazy val credentialsProvider = config.getString("credentialsProvider")
}
