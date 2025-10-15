package com.gu.media.youtube

import com.gu.contentatom.thrift.atom.media.{
  Category => ThriftMediaCategory,
  MediaAtom => ThriftMediaAtom
}
import com.gu.media.youtube.MediaAtomYoutubeDescriptionHandler.getYoutubeDescription
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers

class MediaAtomYoutubeDescriptionHandlerTest extends AnyFunSuite with Matchers {

  val thriftAtomData = ThriftMediaAtom(
    title = "a title",
    category = ThriftMediaCategory.News
  )

  test("should return None if there was no text in HTML") {
    val thriftAtomDataWithHtmlWithoutText =
      thriftAtomData.copy(description = Some("<p><br></p>"))
    val actual = getYoutubeDescription(thriftAtomDataWithHtmlWithoutText)
    actual must be(None)
  }

  test("should extract description from html template") {
    val thriftAtomDataWithDescriptionInHtml =
      thriftAtomData.copy(description = Some("<p>test title</p>"))
    val actual = getYoutubeDescription(thriftAtomDataWithDescriptionInHtml)
    actual must be(Some("test title"))
  }

}
