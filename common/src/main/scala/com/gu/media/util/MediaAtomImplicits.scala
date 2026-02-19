package com.gu.media.util

import com.gu.atom.util._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media._
import com.gu.media.model.{SelfHostedOutput, VideoOutput, YouTubeOutput}

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
    val assets = getActiveAssets(atom)
    val posterUrl = atom.posterUrl

    (assets, posterUrl) match {
      case (Nil, Some(poster)) =>
        s"""<img src="$poster"/>"""
      case (assets, poster) =>
        val youtubeAssets = assets.collect { case asset: YouTubeOutput => asset }
        val selfHostedAssets = assets.collect { case asset: SelfHostedOutput => asset }

        if(selfHostedAssets.nonEmpty) {
          s"""
             |<video controls="controls" preload="metadata" ${if (
            poster.isDefined
          ) s"""poster="${poster.get}""""}>
             | ${selfHostedAssets
            .map(s => s"""<source type="${s.mimeType}" src="${s.id}"/>""")
            .mkString}
             |</video>
        """.stripMargin
        } else if (youtubeAssets.nonEmpty) {
            s"""<iframe frameborder="0" allowfullscreen="true" src="https://www.youtube-nocookie.com/embed/${youtubeAssets.head.id}?showinfo=0&rel=0"></iframe>"""
        } else {
          "<div />"
        }
      case _ =>
        "<div />"
    }
  }

  private def getActiveAssets(atom: MediaAtom): List[VideoOutput] = {
    atom.activeVersion match {
      case Some(version) =>
        val filteredAssets = atom.assets.filter(_.version == version).toList
        filteredAssets.flatMap {
          case Asset(_, _, id, Platform.Youtube, _, _, _) =>
            Some(YouTubeOutput(id))
          case Asset(_, _, id, Platform.Url, Some(mimeType), _, _) =>
            Some(SelfHostedOutput(id, mimeType))
          case _ => None
        }
      case None => Nil
    }
  }
}

object MediaAtomImplicits extends MediaAtomImplicits
