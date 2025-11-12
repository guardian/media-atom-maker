package com.gu.media

import com.gu.media.model.{
  ContentChangeDetails,
  Category => AppCategory,
  MediaAtom => AppMediaAtom
}

object TestHelpers {

  val emptyAppMediaAtom: AppMediaAtom = AppMediaAtom(
    id = "test",
    labels = List.empty,
    contentChangeDetails = ContentChangeDetails(
      lastModified = None,
      created = None,
      published = None,
      revision = 1L,
      scheduledLaunch = None,
      embargo = None,
      expiry = None
    ),
    assets = List.empty,
    activeVersion = None,
    title = "test",
    category = AppCategory.News,
    plutoData = None,
    duration = None,
    source = None,
    description = None,
    trailText = None,
    posterImage = None,
    trailImage = None,
    youtubeOverrideImage = None,
    tags = List.empty,
    byline = List.empty,
    commissioningDesks = List.empty,
    keywords = List.empty,
    youtubeCategoryId = None,
    license = None,
    channelId = None,
    legallySensitive = None,
    sensitive = None,
    privacyStatus = None,
    expiryDate = None,
    youtubeTitle = "test",
    youtubeDescription = None,
    composerCommentsEnabled = None,
    optimisedForWeb = None,
    suppressRelatedContent = None
  )
}
