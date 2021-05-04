import { parseComposerDataFromImage } from './parseGridMetadata';
import { getStore } from '../util/storeAccessor';
import { impossiblyDistantDate }  from '../constants/dates';
import VideoUtils from '../util/video';

export function getComposerData(video) {

  const isTrainingMode = getStore().getState().config.isTrainingMode;

  const expiryDate = video.contentChangeDetails && video.contentChangeDetails.expiry && video.contentChangeDetails.expiry.date
  const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
  const embargo = VideoUtils.getEmbargo(video);
  const isEmbargoedIndefinitely = isTrainingMode || (embargo && embargo >= impossiblyDistantDate);
  const embargoedUntil = embargo && embargo < impossiblyDistantDate ? embargo : null;

  return {
    headline: video.title,
    standfirst: video.description ? video.description : null,
    trailText: video.trailText ? video.trailText : null,
    linkText: video.title,
    sensitive: video.sensitive,
    legallySensitive: video.legallySensitive,
    blockAds: video.blockAds,
    commentable: video.composerCommentsEnabled,
    relatedContentOff: video.suppressRelatedContent,
    seoOptimised: video.optimisedForWeb,
    commissioningDesks: video.commissioningDesks.join('|'),
    byline: video.byline.join('|'),
    keywords: video.keywords.join('|'),
    thumbnail: video.trailImage && video.trailImage.assets.length > 0
      ? parseComposerDataFromImage(video.trailImage, video.trailText)
      : null,
    expiryDate: expiryDate,
    scheduledLaunch: scheduledLaunch,
    requestedScheduledLaunch: scheduledLaunch,
    embargoedUntil: embargoedUntil,
    embargoedIndefinitely: isEmbargoedIndefinitely
  };
}
