# YouTube

For testing purposes, we have a developer YouTube channel. You can grant other users access to the testing channel in the [Brand Accounts dashboard](https://myaccount.google.com/brandaccounts) on YouTube. If you don't have access, speak to a member of the [Editorial Tools team](https://github.com/orgs/guardian/teams/digital-cms/members).

Without access to the channel dashboard itself, YouTube information about any given video can also be accessed using an API we expose at `.../api/youtube/video-info/<youtube-id-here>`.

## Managing ads

Although YouTube's monetisation settings can be managed via the [channel dashboard](https://studio.youtube.com), media-atom-maker applies monetisation options when a video is created or updated. This means that settings or default values configured in the channel itself will not be applied to any videos that are managed through media-atom-maker.

We configure the types of ads that we want to apply using the [`YouTubePartnerAPI`](https://github.com/guardian/media-atom-maker/blob/2d2c21f29514f9126cf6a51aa6b9348ead4056b8/common/src/main/scala/com/gu/media/youtube/YouTubePartnerApi.scala#L176). This means that each video has the same options applied, without any need for the options to be configured by the person creating or updating the video.

For more detail on the video advertising options, you can see [YouTube's Content ID API documentation](https://developers.google.com/youtube/partner/docs/v1/videoAdvertisingOptions#properties).

