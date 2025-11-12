package model.commands

import com.gu.media.TestHelpers
import com.gu.media.model.{Category, ContentChangeDetails, MediaAtom}
import model.commands.UpdateAtomCommand.createDiffString
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class UpdateAtomCommandTest extends AnyFunSuite with Matchers {
  val mediaAtomFixture: MediaAtom = TestHelpers.emptyAppMediaAtom.copy(
    id = "123",
    activeVersion = Some(1),
    title = "title",
    description = Some("Example description"),
    duration = Some(1),
    source = Some("source"),
    youtubeTitle = "title"
  )

  test("Diff output when nothing changes") {
    createDiffString(mediaAtomFixture, mediaAtomFixture) must be(
      "Updated atom fields"
    )
  }

  test("Diff output when description changes") {
    createDiffString(
      mediaAtomFixture,
      mediaAtomFixture.copy(description = Some("New description"))
    ) must be(
      "Updated atom fields (description: Example description -> New description)"
    )
  }

  test("Diff output when description is removed") {
    createDiffString(
      mediaAtomFixture,
      mediaAtomFixture.copy(description = None)
    ) must be(
      "Updated atom fields (description: Example description -> [NONE])"
    )
  }
}
