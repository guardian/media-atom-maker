package com.gu.media.util

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import org.scalatest.{FunSuite, MustMatchers}
import MediaAtomHelpers._
import com.gu.media.model.{SelfHostedAsset, VideoSource, YouTubeAsset}

class MediaAtomHelpersTest extends FunSuite with MustMatchers {
  test("add YouTube asset") {
    val newAtom = updateAtom(atom()) { mediaAtom =>
      addAsset(mediaAtom, YouTubeAsset("L9CMNVzMHJ8", Some("16:9")), version = 2)
    }

    val expected = Seq(
      asset().copy(platform = Platform.Youtube, id = "L9CMNVzMHJ8", aspectRatio = Some("16:9"), version = 2),
      asset()
    )

    newAtom.contentChangeDetails.revision must be(2)
    assets(newAtom) must be(expected)
  }

  test("add self hosted asset") {
    val newAsset = SelfHostedAsset(List(
      VideoSource("test.mp4", "video/mp4"),
      VideoSource("test.m3u8", "video/m3u8"))
    )

    val newAtom = updateAtom(atom()) { mediaAtom =>
      addAsset(mediaAtom, newAsset, version = 2)
    }

    val expected = Seq(
      asset().copy(platform = Platform.Url, id = "test.mp4", version = 2, mimeType = Some("video/mp4")),
      asset().copy(platform = Platform.Url, id = "test.m3u8", version = 2, mimeType = Some("video/m3u8")),
      asset()
    )

    newAtom.contentChangeDetails.revision must be(2)
    assets(newAtom) must be(expected)
  }

  private def assets(atom: Atom): Seq[Asset] = {
    atom.data.asInstanceOf[AtomData.Media].media.assets
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
    data = AtomData.Media(MediaAtom(
      assets = Seq(asset()),
      activeVersion = Some(1),
      title = "test",
      category = Category.Feature
    )),
    contentChangeDetails = ContentChangeDetails(revision = 1)
  )
}
