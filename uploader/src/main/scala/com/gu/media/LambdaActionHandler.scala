package com.gu.media

import com.gu.media.aws.AwsAccess.UploaderAccess
import com.gu.media.upload.UploadsDataStore
import com.gu.media.upload.actions.UploadActionHandler
import com.gu.media.youtube.YouTubeUploader
import com.squareup.okhttp._
import play.api.libs.json.{JsArray, JsValue, Json}
import scala.collection.JavaConverters._

class LambdaActionHandler(store: UploadsDataStore, plutoStore: PlutoDataStore, aws: UploaderAccess,
                          youTube: YouTubeUploader, hmac: HmacRequestSupport)
  extends UploadActionHandler(store, plutoStore, aws, youTube) {

  private val actions = new AddAssetActions(aws, hmac)

  override def addAsset(atomId: String, videoId: String): Long = {
    actions.addAsset(atomId, videoId)
  }
}
