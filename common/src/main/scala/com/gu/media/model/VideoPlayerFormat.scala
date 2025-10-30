package com.gu.media.model

import com.gu.contentatom.thrift.atom.media.{
  VideoPlayerFormat => ThriftVideoPlayerFormat
}
import play.api.libs.json._

sealed trait VideoPlayerFormat {
  def name: String
  def asThrift = ThriftVideoPlayerFormat.valueOf(name).get
}

object VideoPlayerFormat {
  case object Default extends VideoPlayerFormat { val name = "Default" }
  case object Loop extends VideoPlayerFormat { val name = "Loop" }
  case object Cinemagraph extends VideoPlayerFormat { val name = "Cinemagraph" }

  val videoPlayerFormatReads = Reads[VideoPlayerFormat](json => {
    json.as[String] match {
      case "Default"     => JsSuccess(Default)
      case "Loop"        => JsSuccess(Loop)
      case "Cinemagraph" => JsSuccess(Cinemagraph)
    }
  })

  val videoPlayerFormatWrites: Writes[VideoPlayerFormat] =
    Writes[VideoPlayerFormat](vpf => {
      JsString(vpf.name)
    })

  implicit val videoPlayerFormatFormat: Format[VideoPlayerFormat] =
    Format(videoPlayerFormatReads, videoPlayerFormatWrites)

  private val types = List(Default, Loop, Cinemagraph)

  def fromThrift(p: ThriftVideoPlayerFormat) = types.find(_.name == p.name).get
}
