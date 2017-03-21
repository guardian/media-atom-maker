package util

import com.gu.media.upload.actions.{S3UploadAccess, UploadActionHandler}
import com.gu.media.youtube.{YouTube, YouTubeUploader}
import com.gu.pandomainauth.model.{User => PandaUser}
import data.DataStores
import model.commands.AddAssetCommand

/**
  * For use in dev, uploads the video and adds the asset directly rather than via a Lambda
  */
class DevUploadHandler(stores: DataStores, s3: S3UploadAccess, youTube: YouTube)
  extends UploadActionHandler(stores.uploadStore, s3, new YouTubeUploader(s3, youTube)) {

  private val user = PandaUser("Media", "Atom Maker", "media-atom-maker@theguardian.co.uk", None)

  override def addAsset(atomId: String, videoId: String): Long = {
    val atom = AddAssetCommand(atomId, videoId, stores, youTube, user).process()
    val versions = atom.assets.map(_.version).sorted

    versions.last
  }
}
