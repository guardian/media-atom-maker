import { parseComposerDataFromImage } from './parseGridMetadata';
import { getStore } from '../util/storeAccessor';

function asBooleanString(value) {
  return value ? 'true' : 'false';
}

export function getComposerData(video, videoBlock) {
  const coreFields = [
    {
      'headline': video.title
    },
    {
      'standfirst': video.description ? video.description : null
    },
    {
      'trailText': video.trailText ? video.trailText : null
    },
    {
      'linkText': video.title
    },
    {
      'sensitive': asBooleanString(video.sensitive)
    },
    {
      'legallySensitive': asBooleanString(video.legallySensitive)
    },
    {
      'blockAds': asBooleanString(video.blockAds)
    },
    {
      'commentable': asBooleanString(video.composerCommentsEnabled)
    },
    {
      'relatedContentOff': asBooleanString(video.suppressRelatedContent)
    },
    {
      'seoOptimised': asBooleanString(video.optimisedForWeb)
    },
    {
      'byline': video.byline.join('|')
    },
    {
      'commissioningDesks': video.commissioningDesks.join('|')
    },
    {
      'keywords': video.keywords.join('|')
    },
    {
      'thumbnail': (video.trailImage && video.trailImage.assets.length > 0)
        ? parseComposerDataFromImage(video.trailImage, video.trailText)
        : null
    },
    {
      'expiryDate': video.expiryDate
    }
  ];

  const isTrainingMode = getStore().getState().config.isTrainingMode;

  // block training content being published
  const embargoedIndefinately = {
    'embargoedIndefinitely': asBooleanString(true)
  };

  return !isTrainingMode ? coreFields : [...coreFields, embargoedIndefinately];
}
