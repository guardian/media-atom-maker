package com.gu.media.youtube

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class YoutubeDescriptionTest extends AnyFunSuite with Matchers {
  test("cleaning nothing") {
    YoutubeDescription.clean(None) must be(None)
  }

  test("cleaning a plain string") {
    val testString = Some("hello there, how are you?")
    YoutubeDescription.clean(testString) must be(testString)
  }

  test("cleaning a blob of html") {
    val testHtml = Some(
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
    )
    val expected = Some(
      "The three-year construction of Tottenham Hotspur's new stadium is revealed in a time-lapse video released by the club. The £1bn stadium will be officially unveiled on Sunday, before Spurs play their first match at their new home on 3 April against Crystal Palace. \n Tottenham announce first match for new 62,000 capacity stadium"
    )

    YoutubeDescription.clean(testHtml) must be(expected)
  }

  test("ignore html without text") {
    val htmlWithoutText = Some("<p><br></p>")
    YoutubeDescription.clean(htmlWithoutText) must be(None)
  }
}
