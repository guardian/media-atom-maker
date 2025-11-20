import { parseComposerDataFromImage } from './parseGridMetadata';
import { getStore } from './storeAccessor';
import { impossiblyDistantDate }  from '../constants/dates';
import VideoUtils from './video';
import moment from 'moment';

export const getDateAsNumber = (date) => {
  if (typeof date === "string"){
    return moment(date).valueOf();
  }
  else return date;
};

export function getComposerData(video) {

  const isTrainingMode = getStore().getState().config.isTrainingMode;
  const expiryDate = video.contentChangeDetails && video.contentChangeDetails.expiry && video.contentChangeDetails.expiry.date;
  const cleanedExpiryDate = getDateAsNumber(expiryDate);
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
    expiryDate: cleanedExpiryDate,
    scheduledLaunch: scheduledLaunch,
    requestedScheduledLaunch: scheduledLaunch,
    embargoedUntil: embargoedUntil,
    embargoedIndefinitely: isEmbargoedIndefinitely,
    videoPlayerFormat: video.videoPlayerFormat,
    platform: video.platform
  };
}

export function getComposerId() {
  const usages = getStore().getState().usage.data;
  const videoPages = [...usages.preview.video, ...usages.published.video];
  if (videoPages.length !== 0) {
    return videoPages[0].fields.internalComposerCode;
  }
  else {
    console.log("Could not find composer id");
  }
}
