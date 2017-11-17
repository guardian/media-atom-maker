package com.gu.media

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.editorial.permissions.client._
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.Future

case class Permissions(deleteAtom: Boolean, addSelfHostedAsset: Boolean, setVideosOnAllChannelsPublic: Boolean)
object Permissions {
  implicit val format: Format[Permissions] = Jsonx.formatCaseClass[Permissions]

  val app = "atom-maker"
  val deleteAtom = Permission("delete_atom", app, defaultVal = PermissionDenied)
  val addSelfHostedAsset = Permission("add_self_hosted_asset", app, defaultVal = PermissionDenied)
  val setVideosOnAllChannelsPublic = Permission("set_videos_on_all_channels_public", app, defaultVal = PermissionDenied)
}

class MediaAtomMakerPermissionsProvider(stage: String, credsProvider: AWSCredentialsProvider) extends PermissionsProvider {
  import Permissions._

  val all = Seq(deleteAtom, addSelfHostedAsset, setVideosOnAllChannelsPublic)
  val none = Permissions(deleteAtom = false, addSelfHostedAsset = false, setVideosOnAllChannelsPublic = false )

  implicit def config = PermissionsConfig(
    app = "media-atom-maker",
    all = Seq(deleteAtom, addSelfHostedAsset, setVideosOnAllChannelsPublic),
    s3BucketPrefix = if(stage == "PROD") "PROD" else "CODE",
    awsCredentials = credsProvider
  )

  def getAll(email: String): Future[Permissions] = for {
    deleteAtom <- hasPermission(deleteAtom, email)
    selfHostedMediaAtom <- hasPermission(addSelfHostedAsset, email)
    publicStatusPermissions <- hasPermission(setVideosOnAllChannelsPublic, email)
  } yield Permissions(deleteAtom, selfHostedMediaAtom, publicStatusPermissions)


  def getUploadPermissions(email: String): Future[Permissions] = for {
    selfHostedMediaAtom <- hasPermission(addSelfHostedAsset, email)
  } yield {
    Permissions(deleteAtom = false, selfHostedMediaAtom, setVideosOnAllChannelsPublic = false)
  }

  def getStatusPermissions(email: String): Future[Permissions] = for {
    publicStatus <- hasPermission(setVideosOnAllChannelsPublic, email)
  } yield {
    Permissions(deleteAtom = false, addSelfHostedAsset = false, publicStatus)
  }

  private def hasPermission(permission: Permission, email: String): Future[Boolean] = {
    get(permission)(PermissionsUser(email)).map {
      case PermissionGranted => true
      case _ => false
    }
  }
}
