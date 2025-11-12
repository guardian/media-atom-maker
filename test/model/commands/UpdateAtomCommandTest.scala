package model.commands

import com.gu.media.TestHelpers
import com.gu.media.model.{Category, ContentChangeDetails, MediaAtom}
import model.commands.UpdateAtomCommand.{
  createDiffString,
  shouldNotifyThirdPartyServices
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class UpdateAtomCommandTest extends AnyFlatSpec with Matchers {
  val mediaAtomFixture: MediaAtom = TestHelpers.emptyAppMediaAtom.copy(
    id = "123",
    activeVersion = Some(1),
    title = "title",
    description = Some("Example description"),
    duration = Some(1),
    source = Some("source"),
    youtubeTitle = "title"
  )

  behavior of "createDiffString"
  it should "Diff output when nothing changes" in {
    createDiffString(mediaAtomFixture, mediaAtomFixture) must be(
      "Updated atom fields"
    )
  }

  it should "Diff output when description changes" in {
    createDiffString(
      mediaAtomFixture,
      mediaAtomFixture.copy(description = Some("New description"))
    ) must be(
      "Updated atom fields (description: Example description -> New description)"
    )
  }

  it should "Diff output when description is removed" in {
    createDiffString(
      mediaAtomFixture,
      mediaAtomFixture.copy(description = None)
    ) must be(
      "Updated atom fields (description: Example description -> [NONE])"
    )
  }

  behavior of "shouldNotifyThirdPartyServices"
  it should "return true when the old id is not defined a new one is" in {
    shouldNotifyThirdPartyServices(None, Some("id")) must be(true)
  }
  it should "return true when the old id and new id are both defined and different" in {
    shouldNotifyThirdPartyServices(Some("id1"), Some("id2")) must be(true)
  }
  it should "return false when the old id and new id are the same" in {
    shouldNotifyThirdPartyServices(Some("id"), Some("id")) must be(false)
  }
  it should "return false when new id is not defined" in {
    shouldNotifyThirdPartyServices(Some("id1"), None) must be(false)
  }
}
