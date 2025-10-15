package com.gu.media.youtube

import org.jsoup.Jsoup

object YoutubeDescription {
  def clean(maybeDirtyDescription: Option[String]): Option[String] = {
    val desc = maybeDirtyDescription.map(dirtyDescription => {
      val html = Jsoup.parse(dirtyDescription)

      // Extracting the text removes line breaks
      // We add them back in before each paragraph except
      // for the first and before each list element
      html
        .select("p:gt(0), li")
        .prepend("\\n")
        .select("a")
        .unwrap()

      html.text().replace("\\n", "\n")
    })

    /** We do this additional check if Option is defined and if ti contain empty
      * string Ignore it and return None because if we will return Some("") we
      * will have problems with putting that data into DynamoDB as DynamoDB does
      * not allow empty string and will throw exception: Dynamo was unable to
      * process this request. Error message One or more parameter values were
      * invalid: An AttributeValue may not contain an empty string
      */
    if (desc.isDefined && desc.get.isEmpty) return None
    desc
  }
}
