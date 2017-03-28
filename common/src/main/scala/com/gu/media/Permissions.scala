package com.gu.media

import com.gu.editorial.permissions.client._
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.Future

case class Permissions(addAsset: Boolean, deleteAtom: Boolean)

object Permissions extends PermissionsProvider {
  implicit val format: Format[Permissions] = Jsonx.formatCaseClass[Permissions]

  val app = "media-atom-maker"
  val addAsset = Permission("media_atom_add_asset", app, defaultVal = PermissionDenied)
  val deleteAtom = Permission("media_atom_delete", app, defaultVal = PermissionDenied)

  val all = Seq(addAsset, deleteAtom)
  val none = Permissions(addAsset = false, deleteAtom = false)

  implicit def config = PermissionsConfig(app, all)

  def get(email: String): Future[Permissions] = for {
    addAsset <- canAddAsset(email)
    deleteAtom <- canDeleteAtom(email)
  } yield Permissions(addAsset, deleteAtom)

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
