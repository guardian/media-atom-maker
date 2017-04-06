package com.gu.media

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.editorial.permissions.client._
import org.cvogt.play.json.Jsonx
import play.api.libs.json.Format

import scala.concurrent.Future

case class Permissions(addAsset: Boolean, deleteAtom: Boolean)
object Permissions {
  implicit val format: Format[Permissions] = Jsonx.formatCaseClass[Permissions]

  val app = "media-atom-maker"
  val addAsset = Permission("add_media_atom_asset", app, defaultVal = PermissionDenied)
  val deleteAtom = Permission("delete_media_atom", app, defaultVal = PermissionDenied)
}

class MediaAtomMakerPermissionsProvider(stage: String, credsProvider: AWSCredentialsProvider) extends PermissionsProvider {
  import Permissions._

  val all = Seq(addAsset, deleteAtom)
  val none = Permissions(addAsset = false, deleteAtom = false)

  implicit def config = PermissionsConfig(
    app = "media-atom-maker",
    all = Seq(addAsset, deleteAtom),
    s3BucketPrefix = if(stage == "PROD") "PROD" else "CODE",
    awsCredentials = credsProvider
  )

  def getAll(email: String): Future[Permissions] = for {
    addAsset <- hasPermission(addAsset, email)
    deleteAtom <- hasPermission(deleteAtom, email)
  } yield Permissions(addAsset, deleteAtom)

  private def hasPermission(permission: Permission, email: String): Future[Boolean] = {
    get(permission)(PermissionsUser(email)).map {
      case PermissionGranted => true
      case _ => false
    }
  }
}
