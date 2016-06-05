package test

import com.gu.contentatom.thrift.{ ContentAtomEvent, _ }
import com.gu.contentatom.thrift.atom.media._
import java.util.Date

object TestData {
  val testAtom = Atom(
    id = "1",
    atomType = AtomType.Media,
    defaultHtml = "<div></div>",
    data = AtomData.Media(
      MediaAtom(
        activeVersion = 1L
      )
    ),
    contentChangeDetails = ContentChangeDetails(revision = 1)
  )

  def testAtomEvent(atom: Atom = testAtom) =
    ContentAtomEvent(testAtom, EventType.Update, (new Date()).getTime())
}
