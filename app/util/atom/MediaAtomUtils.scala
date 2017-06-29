package util.atom

import com.gu.atom.util._
import com.gu.contentatom.thrift._
import com.gu.contentatom.thrift.atom.media.Platform.{Url, Youtube}
import com.gu.contentatom.thrift.atom.media._
import com.gu.media.model.{SelfHostedAsset, VideoAsset, VideoSource, YouTubeAsset}

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

  def defaultMediaHtml(atom: MediaAtom): String = {
    val asset = getActiveAsset(atom)
    val posterUrl = atom.posterUrl

    views.html.MediaAtom.defaultEmbed(asset, posterUrl).toString()
  }

  private def getActiveAsset(atom: MediaAtom): Option[VideoAsset] = {
    atom.activeVersion.map { version =>
      atom.assets.filter(_.version == version) match {
        case asset :: Nil if asset.platform == Youtube =>
          YouTubeAsset(asset.id)

        case assets =>
          val sources = assets.collect {
            case Asset(_, _, id, _, Some(mimeType)) => VideoSource(id, mimeType)
          }

          SelfHostedAsset(sources.toList)
      }
    }
  }
}

object MediaAtomImplicits extends MediaAtomImplicits
