package model.commands

import com.gu.media.model.{Category, ContentChangeDetails, MediaAtom}
import model.commands.UpdateAtomCommand.createDiffString
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class UpdateAtomCommandTest extends AnyFunSuite with Matchers {
  val mediaAtomFixture: MediaAtom = MediaAtom(
    id = "123",
    labels = List.empty,
    assets = List.empty,
    activeVersion = Some(1),
    title = "title",
    category = Category.News,
    description = Some("Example description"),
    duration = Some(1),
    source = Some("source"),
    posterImage = None,
    trailText = None,
    youtubeTitle = "title",
    youtubeDescription = None,
    trailImage = None,
    tags = List.empty,
    byline = List.empty,
    commissioningDesks = List.empty,
    contentChangeDetails =
      ContentChangeDetails(None, None, None, 1L, None, None, None),
    privacyStatus = None,
    channelId = None,
    youtubeCategoryId = None,
    youtubeOverrideImage = None,
    keywords = List.empty,
    license = None,
    plutoData = None,
    expiryDate = None,
    legallySensitive = None,
    sensitive = None,
    optimisedForWeb = None,
    composerCommentsEnabled = None,
    suppressRelatedContent = None
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
