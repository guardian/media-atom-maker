package com.gu.media.model

import org.scalatest.{FunSuite, MustMatchers}
import com.gu.contentatom.thrift.{AtomData, Atom => ThriftAtom, AtomType => ThriftAtomType, ContentChangeDetails => ThriftContentChangeDetails}
import com.gu.contentatom.thrift.atom.media.{Category => ThriftMediaCategory, MediaAtom => ThriftMediaAtom, Metadata => ThriftMetaData, YoutubeData => ThriftYoutubeData}

class MediaAtomTest extends FunSuite with MustMatchers {
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
      description = Some(
        """
          |<p>
          |  The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club.
          |  The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace.
          |</p>
          |<ul>
          |  <li>
          |    <a href=\"https://www.theguardian.com/football/2019/mar/08/tottenham-new-stadium-first-match-brighton-crystal-palace-champions-league\">
          |      Tottenham announce first match for new 62,000 capacity stadium
          |    </a>
          |  </li>
          |</ul>
        """.stripMargin
      )
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

    val expected = Some("The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club. The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace. \n Tottenham announce first match for new 62,000 capacity stadium")

    mediaAtom.youtubeDescription must be(expected)
  }

  test("test custom youtube description") {
    val thriftAtomData = ThriftMediaAtom(
      title = "a title",
      category = ThriftMediaCategory.News,
      description = Some(
        """
          |<p>
          |  The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club.
          |  The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace.
          |</p>
          |<ul>
          |  <li>
          |    <a href=\"https://www.theguardian.com/football/2019/mar/08/tottenham-new-stadium-first-match-brighton-crystal-palace-champions-league\">
          |      Tottenham announce first match for new 62,000 capacity stadium
          |    </a>
          |  </li>
          |</ul>
        """.stripMargin
      ),
      metadata = Some(ThriftMetaData(
        youtube = Some(ThriftYoutubeData(title = "a title", description = Some("a custom description for youtube")))
      ))
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
