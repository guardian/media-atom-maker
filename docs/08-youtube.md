# YouTube

For testing purposes, we have a developer YouTube channel. You can grant other users access to the testing channel in the [Brand Accounts dashboard](https://myaccount.google.com/brandaccounts) on YouTube. If you don't have access, speak to a member of the [Editorial Tools team](https://github.com/orgs/guardian/teams/digital-cms/members).

Troubleshooting: If you have trouble accessing [studio.youtube.com](studio.youtube.com) after having access granted, try navigating to youtube directly and selecting the switch accounts option under your profile picture.

Without access to the channel dashboard itself, YouTube information about any given video can also be accessed using an API we expose at `.../api/youtube/video-info/<youtube-id-here>`.

## Managing ads

Although YouTube's monetisation settings can be managed via the [channel dashboard](https://studio.youtube.com), media-atom-maker applies monetisation options when a video is created or updated. This means that settings or default values configured in the channel itself will not be applied to any videos that are managed through media-atom-maker.

We configure the types of ads that we want to apply using the [`YouTubePartnerAPI`](https://github.com/guardian/media-atom-maker/blob/2d2c21f29514f9126cf6a51aa6b9348ead4056b8/common/src/main/scala/com/gu/media/youtube/YouTubePartnerApi.scala#L176). This means that each video has the same options applied, without any need for the options to be configured by the person creating or updating the video.

For more detail on the video advertising options, you can see [YouTube's Content ID API documentation](https://developers.google.com/youtube/partner/docs/v1/videoAdvertisingOptions#properties).

## Adding a new YouTube channel in PROD

When editing the YouTube furniture configuration in the UI, the list of available channels is populated via a combination of the YouTube API and the app config.

To add a new channel to the list of available channels in PROD, the following steps need to be taken:

1. Make sure that the channel is being managed via the Guardian's YouTube Content Manager account.
2. Add the channel id to the `youtube.channels.allowed` config for PROD. This is currently stored in the S3 config bucket.
3. Redeploy `main` on PROD to pick up the config change.
