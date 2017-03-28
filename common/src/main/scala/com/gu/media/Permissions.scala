package com.gu.media

import com.gu.editorial.permissions.client._

import scala.concurrent.Future

case class Permissions(addAsset: Boolean, deleteAtom: Boolean)

object Permissions extends PermissionsProvider {
  val app = "media-atom-maker"
  val addAsset = Permission("media_atom_add_asset", app, defaultVal = PermissionDenied)
  val deleteAtom = Permission("media_atom_delete", app, defaultVal = PermissionDenied)

  val all = Seq(addAsset, deleteAtom)
  val none = Permissions(addAsset = false, deleteAtom = false)

  implicit def config = PermissionsConfig(app, all)

  def canAddAsset(email: String): Future[Boolean] = {
    get(addAsset)(PermissionsUser(email)).map {
      case PermissionGranted => true
      case _ => false
    }
  }

  def canDeleteAtom(email: String): Future[Boolean] = {
    get(deleteAtom)(PermissionsUser(email)).map {
      case PermissionGranted => true
      case _ => false
    }
  }
}
