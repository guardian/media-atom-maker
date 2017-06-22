export function getComposerData(video) {
  return [
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
      name: 'sensitive',
      value: video.sensitive ? 'true' : 'false',
      belongsTo: 'settings'
    },
    {
      name: 'legallySensitive',
      value: video.legallySensitive ? 'true' : 'false',
      belongsTo: 'settings'
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
    }
  ];
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
