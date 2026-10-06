package com.gu.media.model

case class MediaAtomPublishSettings(
    atomId: String,
    // When true, this prevents the YouTube title/description from being overwritten on publish
    retainYoutubeFurniture: Boolean = false
)
