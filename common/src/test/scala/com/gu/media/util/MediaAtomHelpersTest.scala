package com.gu.media.util

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{
  Atom,
  AtomData,
  AtomType,
  ContentChangeDetails,
  User
}
import MediaAtomHelpers._
import com.gu.media.model.{SelfHostedAsset, VideoSource, YouTubeAsset}
import org.joda.time.DateTime
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class MediaAtomHelpersTest extends AnyFunSuite with Matchers {
  test("add YouTube asset") {
    val startTime = DateTime.now().getMillis
    val newAtom = updateAtom(atom(), user()) { mediaAtom =>
      addAsset(
        mediaAtom,
        YouTubeAsset("L9CMNVzMHJ8"),
        version = 2,
        hasSubtitles = false
      )
    }

    val expected = Seq(
      asset()
        .copy(platform = Platform.Youtube, id = "L9CMNVzMHJ8", version = 2),
      asset()
    )

    newAtom.contentChangeDetails.lastModified must not be empty
    newAtom.contentChangeDetails.lastModified.get.date must be >= startTime
    newAtom.contentChangeDetails.lastModified.get.user must not be empty
    newAtom.contentChangeDetails.lastModified.get.user.get.email must be(
      "jo.blogs@guardian.co.uk"
    )
    newAtom.contentChangeDetails.lastModified.get.user.get.firstName must contain(
      "Jo"
    )
    newAtom.contentChangeDetails.lastModified.get.user.get.lastName must contain(
      "Blogs"
    )
    newAtom.contentChangeDetails.revision must be(2)
    assets(newAtom) must be(expected)
  }

  test("add self hosted asset") {
    val newAsset = SelfHostedAsset(
      List(
        VideoSource("test.mp4", "video/mp4"),
        VideoSource("test.m3u8", "application/vnd.apple.mpegurl")
      )
    )

    val newAtom = updateAtom(atom(), user()) { mediaAtom =>
      addAsset(mediaAtom, newAsset, version = 2, hasSubtitles = false)
    }

    val expected = Seq(
      asset().copy(
        platform = Platform.Url,
        id = "test_480w.mp4",
        version = 2,
        mimeType = Some("video/mp4")
      ),
      asset().copy(
        platform = Platform.Url,
        id = "test_720h.mp4",
        version = 2,
        mimeType = Some("video/mp4")
      ),
      asset().copy(
        platform = Platform.Url,
        id = "test.m3u8",
        version = 2,
        mimeType = Some("application/vnd.apple.mpegurl")
      ),
      asset()
    )

    newAtom.contentChangeDetails.revision must be(2)
    assets(newAtom) must be(expected)
  }

  test("add self hosted asset with subtitles") {
    val newAsset = SelfHostedAsset(
      List(
        VideoSource("test.mp4", "video/mp4"),
        VideoSource("test.m3u8", "application/vnd.apple.mpegurl")
      )
    )

    val newAtom = updateAtom(atom(), user()) { mediaAtom =>
      addAsset(mediaAtom, newAsset, version = 2, hasSubtitles = true)
    }

    val expected = Seq(
      asset().copy(
        platform = Platform.Url,
        id = "test_480w.mp4",
        version = 2,
        mimeType = Some("video/mp4")
      ),
      asset().copy(
        platform = Platform.Url,
        id = "test_720h.mp4",
        version = 2,
        mimeType = Some("video/mp4")
      ),
      asset().copy(
        platform = Platform.Url,
        id = "test.m3u8",
        version = 2,
        mimeType = Some("application/vnd.apple.mpegurl")
      ),
      asset().copy(
        assetType = AssetType.Subtitles,
        platform = Platform.Url,
        id = "testcaptions_00001.vtt",
        version = 2,
        mimeType = Some("text/vtt")
      ),
      asset()
    )
    newAtom.contentChangeDetails.revision must be(2)
    assets(newAtom) must be(expected)
  }

  test("get user name from email") {
    getUser("first.last@guardian.co.uk") must be(
      User("first.last@guardian.co.uk", Some("First"), Some("Last"))
    )
    getUser("first.last.foo.bar@guardian.co.uk") must be(
      User("first.last.foo.bar@guardian.co.uk", Some("First"), Some("Last"))
    )
    getUser("nodots@guardian.co.uk") must be(
      User("nodots@guardian.co.uk", None, None)
    )
  }

  test("url-encode self-hosted asset keys to urls") {
    val asset = SelfHostedAsset(
      List(
        VideoSource("url encode me.mp4", "video/mp4"),
        VideoSource("url encode me.m3u8", "application/vnd.apple.mpegurl"),
        VideoSource(
          "2025/08/18/My Title--0653ffba-35f4-4883-b961-3139cdaf6c8b-1.0.m3u8",
          "application/vnd.apple.mpegurl"
        )
      )
    )

    urlEncodeSources(asset, "https://gu.com/videos") mustBe SelfHostedAsset(
      List(
        VideoSource("https://gu.com/videos/url+encode+me.mp4", "video/mp4"),
        VideoSource(
          "https://gu.com/videos/url+encode+me.m3u8",
          "application/vnd.apple.mpegurl"
        ),
        VideoSource(
          "https://gu.com/videos/2025/08/18/My+Title--0653ffba-35f4-4883-b961-3139cdaf6c8b-1.0.m3u8",
          "application/vnd.apple.mpegurl"
        )
      )
    )
  }

  private def assets(atom: Atom): Seq[Asset] = {
    atom.data.asInstanceOf[AtomData.Media].media.assets.toSeq
  }

  private def asset(): Asset = Asset(
    assetType = AssetType.Video,
    version = 1,
    id = "test",
    platform = Platform.Youtube,
    mimeType = None
  )

  private def atom(): Atom = Atom(
    id = "test",
    atomType = AtomType.Media,
    labels = Seq.empty,
    defaultHtml = "",
    data = AtomData.Media(
      MediaAtom(
        assets = Seq(asset()),
        activeVersion = Some(1),
        title = "test",
        category = Category.Feature
      )
    ),
    contentChangeDetails = ContentChangeDetails(revision = 1)
  )

  private def user(): User = User(
    "jo.blogs@guardian.co.uk",
    Some("Jo"),
    Some("Blogs")
  )
}
