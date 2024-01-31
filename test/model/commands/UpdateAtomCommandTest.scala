package model.commands

import com.gu.media.model.{Category, ContentChangeDetails, MediaAtom}
import model.commands.UpdateAtomCommand.createDiffString
import org.scalatest.{FunSuite, MustMatchers}

class UpdateAtomCommandTest extends FunSuite with MustMatchers {
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
    contentChangeDetails = ContentChangeDetails(None, None, None, 1L, None, None, None),
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
    createDiffString(mediaAtomFixture, mediaAtomFixture) must be("Updated atom fields (category = News$,, channelId = None,, description = Some( \"Example description\" ),, duration = Some( 1 ),, legallySensitive = None,, license = None,, source = Some( \"source\" ),, title = \"title\",, youtubeCategoryId = None,, youtubeTitle = \"title\")")
  }

  test("Diff output when description changes") {
    createDiffString(mediaAtomFixture, mediaAtomFixture.copy(description = Some("New description"))) must be(
      "Updated atom fields (MediaAtom( ..., description = Some( \u001B\"Example description\"\u001B -> \u001B\"New description\"\u001B ) ))"
    )
  }

  test("Diff output when description is removed") {
    createDiffString(mediaAtomFixture, mediaAtomFixture.copy(description = None)) must be(
      "Updated atom fields (MediaAtom( ..., description = \u001BSome( \"Example description\" )\u001B -> \u001BNone\u001B ))"
    )
  }
}
