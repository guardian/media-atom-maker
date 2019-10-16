package com.gu.media.youtube

import org.jsoup.Jsoup

object YoutubeDescription {
  def clean(maybeDirtyDescription: Option[String]): Option[String] = {
    val desc = maybeDirtyDescription.map(dirtyDescription => {
      val html = Jsoup.parse(dirtyDescription)

      //Extracting the text removes line breaks
      //We add them back in before each paragraph except
      //for the first and before each list element
      html.select("p:gt(0), li")
        .prepend("\\n")
        .select("a")
        .unwrap()

      html.text().replace("\\n", "\n")
    })

    if (desc.isDefined && desc.get.isEmpty) return None
    desc
  }
}
