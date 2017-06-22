package com.gu.media.util

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{Atom, AtomData, AtomType, ContentChangeDetails}
import com.gu.media.util.MediaAtomHelpers._
import org.scalatest.{FunSuite, MustMatchers}

class MediaAtomHelpersTest extends FunSuite with MustMatchers {
  test("add YouTube asset") {
    val newAtom = updateAtom(atom()) { mediaAtom =>
      addAsset(mediaAtom, "L9CMNVzMHJ8")
    }

    val expected = Seq(
      asset().copy(platform = Platform.Youtube, id = "L9CMNVzMHJ8", version = 2),
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
