package com.gu.media

import com.gu.media.youtube.YouTubeAccess

trait ApiStubs { this: YouTubeAccess with CapiPreviewAccess =>
  override def capiPreviewUser = "<not_used>"
  override def capiPreviewPassword = "<not_used>"
  override def capiUrl = "<not_used>"

  override def appName = "<not_used>"
  override def contentOwner = "<not_used>"

  override def clientId = "<not_used>"
  override def clientSecret = "<not_used>"
  override def refreshToken = "<not_used>"
}
