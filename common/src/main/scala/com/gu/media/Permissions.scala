package com.gu.media

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.permissions._
import com.gu.ai.x.play.json.Jsonx
import com.gu.ai.x.play.json.Encoders._
import play.api.libs.json.Format
import com.gu.pandomainauth.model.{User => PandaUser}
import com.gu.permissions.PermissionDefinition

case class Permissions(
    deleteAtom: Boolean = false,
    addSelfHostedAsset: Boolean = false,
    addSubtitles: Boolean = false,
    setVideosOnAllChannelsPublic: Boolean = false,
    pinboard: Boolean = false
)
object Permissions {
  implicit val format: Format[Permissions] = Jsonx.formatCaseClass[Permissions]

  val app = "atom-maker"
  val basicAccess = PermissionDefinition("media_atom_maker_access", app)
  val deleteAtom = PermissionDefinition("delete_atom", app)
  val addSelfHostedAsset = PermissionDefinition("add_self_hosted_asset", app)
  val addSubtitles = PermissionDefinition("add_subtitles", app)
  val setVideosOnAllChannelsPublic =
    PermissionDefinition("set_videos_on_all_channels_public", app)
  val pinboard = PermissionDefinition("pinboard", "pinboard")
}

class MediaAtomMakerPermissionsProvider(
    stage: String,
    region: String,
    credsProvider: AWSCredentialsProvider
) {
  import Permissions._

  private val permissions: PermissionsProvider = PermissionsProvider(
    PermissionsConfig(stage, region, credsProvider)
  )

  def getAll(user: PandaUser): Permissions = Permissions(
    deleteAtom = hasPermission(deleteAtom, user),
    addSelfHostedAsset = hasPermission(addSelfHostedAsset, user),
    addSubtitles = hasPermission(addSubtitles, user),
    setVideosOnAllChannelsPublic =
      hasPermission(setVideosOnAllChannelsPublic, user),
    pinboard = hasPermission(pinboard, user)
  )

  def getStatusPermissions(user: PandaUser): Permissions =
    Permissions(setVideosOnAllChannelsPublic =
      hasPermission(setVideosOnAllChannelsPublic, user)
    )

  def hasPermission(
      permission: PermissionDefinition,
      user: PandaUser
  ): Boolean = user.email match {
    // TODO be better
    // HACK: HMAC authenticated users are a `PandaUser` without an email
    case "" if user.firstName == "media-atom-scheduler-lambda" => true
    case _ => permissions.hasPermission(permission, user.email)
  }

  def hasAnyAtomMakerPermission(user: PandaUser): Boolean = {
    permissions.listPermissions(user.email).exists {
      case (PermissionDefinition(_, app), isActive) =>
        app == Permissions.app && isActive
    }
  }
}
