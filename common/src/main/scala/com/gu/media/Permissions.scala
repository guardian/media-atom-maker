package com.gu.media

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.editorial.permissions.client._
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format
import com.gu.pandomainauth.model.{User => PandaUser}
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

  def getAll(user: PandaUser): Future[Permissions] = for {
    deleteAtom <- hasPermission(deleteAtom, user)
    selfHostedMediaAtom <- hasPermission(addSelfHostedAsset, user)
    publicStatusPermissions <- hasPermission(setVideosOnAllChannelsPublic, user)
  } yield Permissions(deleteAtom, selfHostedMediaAtom, publicStatusPermissions)


  def getUploadPermissions(user: PandaUser): Future[Permissions] = for {
    selfHostedMediaAtom <- hasPermission(addSelfHostedAsset, user)
  } yield {
    Permissions(deleteAtom = false, selfHostedMediaAtom, setVideosOnAllChannelsPublic = false)
  }

  def getStatusPermissions(user: PandaUser): Future[Permissions] = for {
    publicStatus <- hasPermission(setVideosOnAllChannelsPublic, user)
  } yield {
    Permissions(deleteAtom = false, addSelfHostedAsset = false, publicStatus)
  }

  private def hasPermission(permission: Permission, user: PandaUser): Future[Boolean] = {
    user.email match {
      // TODO be better
      // HACK: HMAC authenticated users are a `PandaUser` without an email
      case "" if user.firstName == "media-atom-scheduler-lambda" => {
        Future.successful(true)
      }
      case _ => {
        get(permission)(PermissionsUser(user.email)).map {
          case PermissionGranted => true
          case _ => false
        }
      }
    }
  }
}
