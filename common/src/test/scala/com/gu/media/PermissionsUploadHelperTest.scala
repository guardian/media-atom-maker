package com.gu.media

import com.gu.media.upload.model.Upload.{calculateChunks, oneHundredMegabytes, twoFiveSixKilobytes}
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSuite, MustMatchers}

class PermissionsUploadHelperTest extends FunSuite with MustMatchers {

  test("should allow self-hosted upload if permission granted and selfHosted is true") {
    val permission = Permissions(addAsset = false, deleteAtom = false, addSelfHostedAsset = true)
    PermissionsUploadHelper.canPerformUpload(permission = permission, selfHosted = true) must(be(true))
  }

  test("should not allow self-hosted upload if permission denied and selfHosted is true") {
    val permission = Permissions(addAsset = false, deleteAtom = false, addSelfHostedAsset = false)
    PermissionsUploadHelper.canPerformUpload(permission = permission, selfHosted = true) must(be(false))
  }

  test("should not allow self-hosted upload if permission granted and selfHosted is false") {
    val permission = Permissions(addAsset = false, deleteAtom = false, addSelfHostedAsset = true)
    PermissionsUploadHelper.canPerformUpload(permission = permission, selfHosted = false) must(be(false))
  }

  test("should allow YT upload if permission granted and selfHosted is false") {
    val permission = Permissions(addAsset = true, deleteAtom = false, addSelfHostedAsset = false)
    PermissionsUploadHelper.canPerformUpload(permission = permission, selfHosted = false) must(be(true))
  }

  test("should not allow YT upload if permission denied and selfHosted is false") {
    val permission = Permissions(addAsset = false, deleteAtom = false, addSelfHostedAsset = false)
    PermissionsUploadHelper.canPerformUpload(permission = permission, selfHosted = false) must(be(false))
  }

}
