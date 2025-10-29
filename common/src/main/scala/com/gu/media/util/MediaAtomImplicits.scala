package com.gu.media.util

import com.gu.atom.util._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media.Platform.Youtube
import com.gu.contentatom.thrift.atom.media._
import com.gu.media.model.{
  SelfHostedAsset,
  VideoAsset,
  VideoSource,
  YouTubeAsset
}

trait MediaAtomImplicits extends AtomImplicits[MediaAtom] {
  val dataTyper = new AtomDataTyper[MediaAtom] {
    def getData(a: Atom) = a.data.asInstanceOf[AtomData.Media].media
    def setData(a: Atom, newData: MediaAtom) =
      a.copy(data = a.data.asInstanceOf[AtomData.Media].copy(media = newData))

    def makeDefaultHtml(a: Atom): String = {
      val data = getData(a)
      defaultMediaHtml(data)
    }
  }

  // why not use a Play template? `common` doesn't have a dependency on Play
  def defaultMediaHtml(atom: MediaAtom): String = {
    val asset = getActiveAsset(atom)
    val posterUrl = atom.posterUrl

    (asset, posterUrl) match {
      case (None, Some(poster)) => {
        s"""<img src="$poster"/>"""
      }
      case (Some(YouTubeAsset(id)), _) => {
        s"""<iframe frameborder="0" allowfullscreen="true" src="https://www.youtube-nocookie.com/embed/$id?showinfo=0&rel=0"></iframe>"""
      }
      case (Some(SelfHostedAsset(sources)), poster) => {
        s"""
           |<video controls="controls" preload="metadata" ${if (
            poster.isDefined
          ) s"""poster="${poster.get}""""}>
           | ${sources
            .map(s => s"""<source type="${s.mimeType}" src="${s.src}"/>""")
            .mkString}
           |</video>
        """.stripMargin
      }
      case (None, None) => {
        "<div />"
      }
    }
  }

  private def getActiveAsset(atom: MediaAtom): Option[VideoAsset] = {
    atom.activeVersion.map { version =>
      atom.assets.filter(_.version == version) match {
        case asset :: Nil if asset.platform == Youtube =>
          YouTubeAsset(asset.id)

        case assets =>
          val sources = assets.collect {
            case Asset(_, _, id, _, Some(mimeType), _, _) => VideoSource(id, mimeType)
          }

          SelfHostedAsset(sources.toList)
      }
    }
  }
}

object MediaAtomImplicits extends MediaAtomImplicits
