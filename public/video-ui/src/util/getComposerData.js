import { parseComposerDataFromImage } from './parseGridMetadata';
import { getStore } from '../util/storeAccessor';

function asBooleanString(value) {
  return value ? 'true' : 'false';
}

export function getComposerData(video) {
  const coreFields = [
    {
      name: 'headline',
      value: video.title,
      belongsTo: 'fields',
      isFreeText: true
    },
    {
      name: 'standfirst',
      value: video.description ? video.description : null,
      belongsTo: 'fields',
      isFreeText: true
    },
    {
      name: 'trailText',
      value: video.trailText ? video.trailText : null,
      belongsTo: 'fields',
      isFreeText: true
    },
    {
      name: 'linkText',
      value: video.title,
      belongsTo: 'fields'
    },
    {
      name: 'sensitive',
      value: asBooleanString(video.sensitive),
      belongsTo: 'settings'
    },
    {
      name: 'legallySensitive',
      value: asBooleanString(video.legallySensitive),
      belongsTo: 'settings'
    },
    {
      name: 'blockAds',
      value: asBooleanString(video.blockAds),
      belongsTo: 'settings'
    },
    {
      name: 'commentable',
      value: asBooleanString(video.composerCommentsEnabled),
      belongsTo: 'settings'
    },
    {
      name: 'relatedContentOff',
      value: asBooleanString(video.suppressRelatedContent),
      belongsTo: 'settings'
    },
    {
      name: 'seoOptimised',
      value: asBooleanString(video.optimisedForWeb),
      belongsTo: 'toolSettings'
    },
    {
      name: 'byline',
      value: video.byline.join('|'),
      belongsTo: 'atom'
    },
    {
      name: 'commissioningDesks',
      value: video.commissioningDesks.join('|'),
      belongsTo: 'atom'
    },
    {
      name: 'keywords',
      value: video.keywords.join('|'),
      belongsTo: 'atom'
    },
    {
      name: 'thumbnail',
      value: video.trailImage
        ? parseComposerDataFromImage(video.trailImage, video.trailText)
        : null,
      belongsTo: 'thumbnail'
    }
  ];

  const isTrainingMode = getStore().getState().config.isTrainingMode;

  // block training content being published
  const embargoedIndefinately = {
    name: 'embargoedIndefinitely',
    value: asBooleanString(true),
    belongsTo: 'settings'
  };

  return !isTrainingMode
    ? coreFields
    : [...coreFields, embargoedIndefinately];
}

export function getRightsPayload(video) {
  if (video.expiryDate) {
    return {
      command: 'schedule',
      date: video.expiryDate
    };
  }
  return { command: 'reset' };
}
