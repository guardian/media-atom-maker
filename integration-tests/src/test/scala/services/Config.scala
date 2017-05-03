package integration.services

import com.typesafe.config.{Config, ConfigFactory}

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

  lazy val youTube = YouTubeConfig(config)
  lazy val testVideoBucket = config.getString("testVideos.bucket")
  lazy val testVideo = config.getString("testVideos.video")
}

case class YouTubeConfig(name: String, contentOwner: String, clientId: String, clientSecret: String, refreshToken: String)
object YouTubeConfig {
  def apply(config: Config): YouTubeConfig = YouTubeConfig(
    config.getString("youTube.name"),
    config.getString("youTube.contentOwner"),
    config.getString("youTube.clientId"),
    config.getString("youTube.clientSecret"),
    config.getString("youTube.refreshToken")
  )
}
