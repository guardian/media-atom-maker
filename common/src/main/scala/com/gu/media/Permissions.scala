package com.gu.media

import com.gu.editorial.permissions.client._

import scala.concurrent.Future

case class Permissions(directUpload: Boolean)

object Permissions extends PermissionsProvider {
  val app = "media-atom-maker"
  val directUpload = Permission("media_atom_direct_upload", app, defaultVal = PermissionDenied)

  val all = Seq(directUpload)
  val none = Permissions(false)

  implicit def config = PermissionsConfig(app, all)

  def forUser(email: String): Future[Permissions] = {
    get(directUpload)(PermissionsUser(email)).map {
      case PermissionGranted => Permissions(directUpload = true)
      case _ => none
    }
  }
}
