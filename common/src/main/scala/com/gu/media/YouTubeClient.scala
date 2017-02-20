package com.gu.media

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.typesafe.config.Config
import scala.collection.JavaConverters._

case class YouTubeClient(
                          client: YouTube,
                          credential: GoogleCredential,
                          contentOwner: String,
                          allowedChannels: List[String],
                          disallowedVideos: List[String]
                        )

object YouTubeClient {
  def apply(config: Config): YouTubeClient = {
    lazy val appName = string("name", config)

    lazy val clientId = string("youtube.clientId", config)
    lazy val clientSecret = string("youtube.clientSecret", config)
    lazy val refreshToken = string("youtube.refreshToken", config)
    lazy val contentOwner = string("youtube.contentOwner", config)
    lazy val allowedChannels = stringList("youtube.allowedChannels", config)
    lazy val disallowedVideos = stringList("youtube.disallowedVideos", config)


    val httpTransport = new NetHttpTransport()
    val jacksonFactory = new JacksonFactory()

    val credentials: GoogleCredential = {
      new GoogleCredential.Builder()
        .setTransport(httpTransport)
        .setJsonFactory(jacksonFactory)
        .setClientSecrets(clientId, clientSecret)
        .build
        .setRefreshToken(refreshToken)
    }

    val client = new YouTube.Builder(httpTransport, jacksonFactory, credentials)
      .setApplicationName(appName)
      .build

    YouTubeClient(client, credentials, contentOwner, allowedChannels, disallowedVideos)
  }

  private def string(name: String, config: Config): String = {
    if(config.hasPath(name)) {
      config.getString(name)
    } else {
      throw new IllegalArgumentException(s"Missing config key '$name'")
    }
  }

  private def stringList(name: String, config: Config): List[String] = {
    if(config.hasPath(name)) {
      config.getStringList(name).asScala.toList
    } else {
      List.empty
    }
  }
}
