package model.commands

import com.gu.atom.data.{PublishedDataStore, PreviewDataStore}
import com.gu.atom.play.AtomAPIActions
import com.gu.atom.publish.{LiveAtomPublisher, PreviewAtomPublisher}
import model.{UpdatedMetadata, ImageAsset, MediaAtom}
import util.{YouTubeConfig, YouTubeVideoUpdateApi}
import model.commands.CommandExceptions._

case class PublishAtomCommand(id: String)(implicit val previewDataStore: PreviewDataStore,
                                          val previewPublisher: PreviewAtomPublisher,
                                          val publishedDataStore: PublishedDataStore,
                                          val livePublisher: LiveAtomPublisher,
                                          youtubeConfig: YouTubeConfig) extends Command with AtomAPIActions {
  type T = Unit
  def process(): T = {
    previewDataStore.getAtom(id) match {
      case Some(a) =>
        val atom = MediaAtom.fromThrift(a)
        val api = YouTubeVideoUpdateApi(youtubeConfig)

        updateThumbnail(atom, api)
        api.updateMetadata(id, UpdatedMetadata(atom.description, Some(atom.tags), atom.youtubeCategoryId, atom.license))
        publishAtom(id)

      case None => AtomNotFound
    }
  }

  private def updateThumbnail(atom: MediaAtom, api: YouTubeVideoUpdateApi): Unit = {
    val asset = atom.getActiveAsset.get

    val master = atom.posterImage.flatMap(_.master).get
    val MAX_SIZE = 2000000
    val img: ImageAsset = if (master.size.get < MAX_SIZE) {
      master
    } else {
      // Get the biggest crop which is still less than MAX_SIZE
      atom.posterImage.map(
        _.assets
          .filter(a => a.size.nonEmpty && a.size.get < MAX_SIZE)
          .sortBy(_.size.get).head).get
    }

    api.updateThumbnail(asset.id, img.file, img.mimeType.get)
  }
}
