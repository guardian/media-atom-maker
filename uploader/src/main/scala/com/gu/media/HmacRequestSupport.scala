package com.gu.media

import java.net.URI

import com.gu.hmac.{HMACHeaderValues, HMACHeaders}

trait HmacRequestSupport extends HMACHeaders { this: Settings =>
  final override def secret = getMandatoryString("secret")

  def generateHmacHeaders(uri: String): Map[String, String] = {
    val HMACHeaderValues(date, token) = createHMACHeaderValues(new URI(uri))

    Map(
      "X-Gu-Tools-HMAC-Token" -> token,
      "X-Gu-Tools-HMAC-Date" -> date,
      "X-Gu-Tools-Service-Name" -> "media-atom-uploader"
    )
  }
}
