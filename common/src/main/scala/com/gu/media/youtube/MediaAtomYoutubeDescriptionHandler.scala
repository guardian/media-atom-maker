package com.gu.media.youtube

import com.gu.contentatom.thrift.atom.media.{MediaAtom => ThriftMediaAtom}
import com.gu.media.model.MediaAtom
import com.gu.media.util.MediaAtomImplicits
import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format

object MediaAtomYoutubeDescriptionHandler extends MediaAtomImplicits {

  private implicit val mediaAtomFormat: Format[MediaAtom] =
    Jsonx.formatCaseClass[MediaAtom]

  def getYoutubeDescription(data: ThriftMediaAtom): Option[String] = {

    val youtubeDescription: Option[String] =
      data.metadata.flatMap(_.youtube) match {
        case Some(youtubeData) if youtubeData.description.isDefined =>
          youtubeData.description
        case _ => YoutubeDescription.clean(data.description)
      }

    youtubeDescription
  }

}
