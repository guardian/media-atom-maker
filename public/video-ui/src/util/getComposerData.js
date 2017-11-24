import { parseComposerDataFromImage } from './parseGridMetadata';
import { getStore } from '../util/storeAccessor';

export function getComposerData(video) {
  const isTrainingMode = getStore().getState().config.isTrainingMode;
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
    expiryDate: video.contentChaangeDetails.expiry && video.contentChaangeDetails.expiry.date,
    embargoedIndefinitely: isTrainingMode ? true : false
  };
}
