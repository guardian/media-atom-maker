package com.gu.media.model

import com.gu.contentatom.thrift.{
  AtomData,
  Atom => ThriftAtom,
  AtomType => ThriftAtomType,
  ContentChangeDetails => ThriftContentChangeDetails
}
import com.gu.contentatom.thrift.atom.media.{
  Category => ThriftMediaCategory,
  MediaAtom => ThriftMediaAtom,
  Metadata => ThriftMetaData,
  YoutubeData => ThriftYoutubeData
}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class MediaAtomTest extends AnyFunSuite with Matchers {
  private val htmlDescription =
    """
      |<p>
      |  The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club.
      |  The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace.
      |</p>
      |<ul>
      |  <li>
      |    <a href="https://www.theguardian.com/football/2019/mar/08/tottenham-new-stadium-first-match-brighton-crystal-palace-champions-league">
      |      Tottenham announce first match for new 62,000 capacity stadium
      |    </a>
      |  </li>
      |</ul>
    """.stripMargin

  private val youtubeDescription =
    "The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club. The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace. \n Tottenham announce first match for new 62,000 capacity stadium"

  private def getAtomBeforeCreation(title: String) = MediaAtomBeforeCreation(
    title = title,
    description = None,
    posterImage = None,
    category = Category.News,
    source = None,
    contentChangeDetails =
      ContentChangeDetails(None, None, None, 1L, None, None, None),
    channelId = None,
    privacyStatus = None,
    youtubeCategoryId = None,
    youtubeOverrideImage = None,
    keywords = List.empty,
    license = None,
    blockAds = false,
    expiryDate = None,
    trailImage = None,
    trailText = None,
    tags = List.empty,
    byline = List.empty,
    commissioningDesks = List.empty,
    legallySensitive = None,
    sensitive = None,
    optimisedForWeb = None,
    composerCommentsEnabled = None,
    suppressRelatedContent = None,
    isLoopingVideo = None
  )

  test(
    "test youtube description of MediaAtomBeforeCreation with empty description"
  ) {
    val atomBeforeCreation =
      getAtomBeforeCreation("harry potter and the philosopher's stone")
    atomBeforeCreation.youtubeDescription must be(None)
  }

  test(
    "test youtube description of MediaAtomBeforeCreation with plain description"
  ) {
    val plainDescription = Some("oh hello there")
    val atomBeforeCreation = getAtomBeforeCreation(
      "harry potter and the chamber of secrets"
    ).copy(description = plainDescription)
    atomBeforeCreation.youtubeDescription must be(plainDescription)
  }

  test(
    "test youtube description of MediaAtomBeforeCreation with html description"
  ) {
    val atomBeforeCreation = getAtomBeforeCreation(
      "harry potter and the prisoner of azkaban"
    ).copy(description = Some(htmlDescription))
    val expected = Some(youtubeDescription)
    atomBeforeCreation.youtubeDescription must be(expected)
  }

  test("test generation of youtube title from atom title") {
    val thriftAtomData = ThriftMediaAtom(
      title = "a title",
      category = ThriftMediaCategory.News
    )

    val thriftAtom = ThriftAtom(
      id = "atom-1",
      atomType = ThriftAtomType.Media,
      labels = Seq.empty,
      defaultHtml = "",
      data = AtomData.Media(thriftAtomData),
      contentChangeDetails = ThriftContentChangeDetails(revision = 1L)
    )

    val mediaAtom = MediaAtom.fromThrift(thriftAtom)

    val expected = "a title"

    mediaAtom.youtubeTitle must be(expected)
  }

  test("test generation of youtube description from atom description") {
    val thriftAtomData = ThriftMediaAtom(
      title = "a title",
      category = ThriftMediaCategory.News,
      description = Some(htmlDescription)
    )

    val thriftAtom = ThriftAtom(
      id = "atom-2",
      atomType = ThriftAtomType.Media,
      labels = Seq.empty,
      defaultHtml = "",
      data = AtomData.Media(thriftAtomData),
      contentChangeDetails = ThriftContentChangeDetails(revision = 1L)
    )

    val mediaAtom = MediaAtom.fromThrift(thriftAtom)

    val expected = Some(youtubeDescription)

    mediaAtom.youtubeDescription must be(expected)
  }

  test("test custom youtube description") {
    val thriftAtomData = ThriftMediaAtom(
      title = "a title",
      category = ThriftMediaCategory.News,
      description = Some(htmlDescription),
      metadata = Some(
        ThriftMetaData(
          youtube = Some(
            ThriftYoutubeData(
              title = "a title",
              description = Some("a custom description for youtube")
            )
          )
        )
      )
    )

    val thriftAtom = ThriftAtom(
      id = "atom-3",
      atomType = ThriftAtomType.Media,
      labels = Seq.empty,
      defaultHtml = "",
      data = AtomData.Media(thriftAtomData),
      contentChangeDetails = ThriftContentChangeDetails(revision = 1L)
    )

    val mediaAtom = MediaAtom.fromThrift(thriftAtom)

    val expected = Some("a custom description for youtube")

    mediaAtom.youtubeDescription must be(expected)
  }
}
