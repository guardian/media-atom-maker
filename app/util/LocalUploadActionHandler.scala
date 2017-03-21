package util

import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.{S3UploadAccess, UploadActionHandler}
import com.gu.media.youtube.YouTubeUploader

/**
  * For use in dev, uploads the video and adds the asset directly rather than via a Lambda
  */
class LocalUploadActionHandler(store: UploadsDataStore, s3: S3UploadAccess, youTube: YouTubeUploader)
  extends UploadActionHandler(store, s3, youTube) {

  override def addAsset(atomId: String, videoId: String): Long = {
    -1
  }
}
