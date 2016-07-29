package com.gu.atom.publish.test

import java.util.Date

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{ContentAtomEvent, _}

object TestData {
  val testAtoms = Seq(
    Atom(
      id = "1",
      atomType = AtomType.Media,
      defaultHtml = "<div></div>",
      data = AtomData.Media(
        MediaAtom(
          activeVersion = Some(2L),
          title = "Test atom 1",
          category = Category.Feature,
          assets = List(
            Asset(
              assetType = AssetType.Video,
              version = 1L,
              id = "xyzzy",
              platform = Platform.Youtube
            ),
            Asset(
              assetType = AssetType.Video,
              version = 2L,
              id = "fizzbuzz",
              platform = Platform.Youtube
            )
          ),
          title = "title",
          category = Category.News,
          plutoProjectId = None,
          duration = None,
          source = None,
          posterUrl = None
        )
      ),
      contentChangeDetails = ContentChangeDetails(revision = 1)
    ),
    Atom(
      id = "2",
      atomType = AtomType.Media,
      defaultHtml = "<div></div>",
      data = AtomData.Media(
        MediaAtom(
          activeVersion = None,
          title = "Test atom 2",
          category = Category.News,
          activeVersion = Some(1L),
          assets = List(
            Asset(
              assetType = AssetType.Video,
              version = 1L,
              id = "jdklsajflka",
              platform = Platform.Youtube
            ),
            Asset(
              assetType = AssetType.Video,
              version = 2L,
              id = "afkdljlwe",
              platform = Platform.Youtube
            )
          ),
          title = "title",
          category = Category.News,
          plutoProjectId = None,
          duration = None,
          source = None,
          posterUrl = None
        )
      ),
      contentChangeDetails = ContentChangeDetails(revision = 1)
    )
  )

  def testAtomEvents = testAtoms.map(testAtomEvent _)

  val testAtom = testAtoms.head

  def testAtomEvent(atom: Atom = testAtom) =
    ContentAtomEvent(testAtom, EventType.Update, (new Date()).getTime())
}
