package test

import java.util.Date

import com.gu.contentatom.thrift.atom.media._
import com.gu.contentatom.thrift.{ContentAtomEvent, _}

object TestData {
  val testAtom = Atom(
    id = "1",
    atomType = AtomType.Media,
    defaultHtml = "<div></div>",
    data = AtomData.Media(
      MediaAtom(
        activeVersion = Some(2L),
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
        posterUrl = None,
        description = None,
        metadata = None
      )
    ),
    contentChangeDetails = ContentChangeDetails(revision = 1)
  )

  def testAtomEvent(atom: Atom = testAtom) =
    ContentAtomEvent(testAtom, EventType.Update, new Date().getTime)
}
