package com.gu.media

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.editorial.permissions.client._
import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders._
import play.api.libs.json.Format
import com.gu.pandomainauth.model.{User => PandaUser}
import scala.concurrent.Future

case class Permissions(
  deleteAtom: Boolean,
  addSelfHostedAsset: Boolean,
  setVideosOnAllChannelsPublic: Boolean,
  pinboard: Boolean
)
object Permissions {
  implicit val format: Format[Permissions] = Jsonx.formatCaseClass[Permissions]

  val app = "atom-maker"
  val deleteAtom = Permission("delete_atom", app, defaultVal = PermissionDenied)
  val addSelfHostedAsset = Permission("add_self_hosted_asset", app, defaultVal = PermissionDenied)
  val setVideosOnAllChannelsPublic = Permission("set_videos_on_all_channels_public", app, defaultVal = PermissionDenied)
  val pinboard = Permission("pinboard", "pinboard", defaultVal = PermissionDenied)
}

class MediaAtomMakerPermissionsProvider(stage: String, credsProvider: AWSCredentialsProvider) extends PermissionsProvider {
  import Permissions._

  implicit def config = PermissionsConfig(
    app = "media-atom-maker",
    all = Seq(deleteAtom, addSelfHostedAsset, setVideosOnAllChannelsPublic, pinboard),
    s3BucketPrefix = if(stage == "PROD") "PROD" else "CODE",
    awsCredentials = credsProvider
  )

  def getAll(user: PandaUser): Future[Permissions] = for {
    deleteAtom <- hasPermission(deleteAtom, user)
    selfHostedMediaAtom <- hasPermission(addSelfHostedAsset, user)
    publicStatusPermissions <- hasPermission(setVideosOnAllChannelsPublic, user)
    pinboard <- hasPermission(pinboard, user)
  } yield Permissions(deleteAtom, selfHostedMediaAtom, publicStatusPermissions, pinboard)

  def getStatusPermissions(user: PandaUser): Future[Permissions] = for {
    publicStatus <- hasPermission(setVideosOnAllChannelsPublic, user)
  } yield {
    Permissions(deleteAtom = false, addSelfHostedAsset = false, publicStatus, pinboard = false)
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
