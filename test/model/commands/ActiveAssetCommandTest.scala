package model.commands

import com.gu.media.model.Platform.{Url, Youtube}
import com.gu.media.model._
import com.gu.pandomainauth.model.User
import data.DataStores
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import util.{AWSConfig, ActivateAssetRequest, ImageUtil, YouTube}

class ActiveAssetCommandTest extends AnyFlatSpec with Matchers {

  val atomId = "ace3fcf6-1378-41db-9d21-f3fc07072ab2"
  val stores = mock[DataStores]
  val youtube = mock[YouTube]
  val user = mock[User]
  val awsConfig = mock[AWSConfig]
  val imageUtil = mock[ImageUtil]

  when(imageUtil.getS3ImageAsset(anyString, anyString, anyString)).thenReturn(
    image("https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-2.0.0000000.jpg").master
  )

  val command = ActiveAssetCommand(atomId, ActivateAssetRequest(atomId, 3L), stores, youtube, user, awsConfig, imageUtil)

  "firstFrameImageName" should "return the name of the first frame image associated with an mp4 video" in {
    val mp4Name = "Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-3.0.mp4"
    val expected = "Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-3.0.0000000.jpg"
    command.firstFrameImageName(mp4Name) shouldBe expected
  }

  "autoFirstFrameImage" should "return the first frame of the requested video version, when there is no active video" in {
    val atom = mediaAtom(activeVersion = None)
    val newVersion: Long = 2L

    val expected: Option[Image] = Some(
      image("https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-2.0.0000000.jpg")
    )
    val actual = command.autoFirstFrameImage(atom, newVersion)

    actual shouldEqual expected
  }

  it should "return the first frame of the requested video version, when the active video has an auto-populated posterImage" in {
    // version 1 is active with auto-populated poster image
    val atom = mediaAtom(activeVersion = Some(1L), posterImage = Some(
      image("https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-1.0.0000000.jpg")
    ))
    val newVersion: Long = 2L

    val expected: Option[Image] = Some(
      image("https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-2.0.0000000.jpg")
    )
    val actual = command.autoFirstFrameImage(atom, newVersion)

    actual shouldEqual expected
  }

  it should "return None, when the selected asset version is not self-hosted" in {
    val atom = mediaAtom(activeVersion = None, assets = youTubeAssets())
    val newVersion: Long = 2L

    val expected: Option[Image] = None
    val actual = command.autoFirstFrameImage(atom, newVersion)

    actual shouldEqual expected
  }

  it should "return None, when the first frame image doesn't exist" in {
    when(imageUtil.getS3ImageAsset(anyString, anyString, anyString)).thenReturn(None)

    val atom = mediaAtom(activeVersion = None)
    val newVersion: Long = 2L

    val expected: Option[Image] = None
    val actual = command.autoFirstFrameImage(atom, newVersion)

    actual shouldEqual expected

  }

  it should "return None, when the active video has a custom posterImage" in {
    // version 1 is active with auto-populated poster image
    val atom = mediaAtom(activeVersion = Some(1L), posterImage = Some(
      image("https://media.gu.com/something-else.jpg")
    ))
    val newVersion: Long = 2L

    val expected: Option[Image] = None
    val actual = command.autoFirstFrameImage(atom, newVersion)

    actual shouldEqual expected
  }


  private def mediaAtom( id: String = "ace3fcf6-1378-41db-9d21-f3fc07072ab2",
                         title: String = "Loop: Japan fireball",
                         activeVersion: Option[Long] = None,
                         posterImage: Option[Image] = None,
                         assets: List[Asset] = selfHostedAssets()
                       ): MediaAtom = MediaAtom(
    id = id,
    labels = Nil,
    contentChangeDetails = ContentChangeDetails(None, None, None, 1L, None, None, None),
    // data field
    assets = assets,
    activeVersion = activeVersion,
    title = title,
    category = Category.News,
    plutoData = None,
    duration = None,
    source = None,
    description = None,
    trailText = None,
    posterImage = posterImage,
    trailImage = None,
    youtubeOverrideImage = None,
    // metadata
    tags = Nil,
    byline = Nil,
    commissioningDesks = Nil,
    keywords = Nil,
    youtubeCategoryId = None,
    license = None,
    channelId = None,
    legallySensitive = None,
    sensitive = None,
    privacyStatus = None,
    expiryDate = None,
    youtubeTitle = "",
    youtubeDescription = None,
  )

  private def selfHostedAssets(): List[Asset] = List(
    Asset(
      AssetType.Video,
      2L,
      "https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-2.0.mp4",
      Url,
      Some("video/mp4")
    ),
    Asset(
      AssetType.Video,
      1L,
      "https://uploads.gu.com/Loop__Japan_fireball--ace3fcf6-1378-41db-9d21-f3fc07072ab2-1.0.mp4",
      Url,
      Some("video/mp4")
    )
  )

  private def youTubeAssets(): List[Asset] = List(
    Asset(
      AssetType.Video,
      2L,
      "acb123",
      Youtube,
      Some("video/mp4")
    ),
    Asset(
      AssetType.Video,
      1L,
      "xyz789",
      Youtube,
      Some("video/mp4")
    )
  )

  private def image(file: String, id: String = "ace3fcf6-1378-41db-9d21-f3fc07072ab2") = {
    val imageAsset = ImageAsset(
      mimeType = Some("image/jpeg"),
      file = file,
      dimensions = None,
      size = None,
      aspectRatio = None
    )
    Image(
      assets = List(imageAsset),
      master = Some(imageAsset),
      mediaId = id,
      source = None
    )
  }
}
