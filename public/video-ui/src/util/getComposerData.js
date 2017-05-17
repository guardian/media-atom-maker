export function getComposerData(video) {
  return [
    {
      name: 'headline',
      value: video.title,
      belongsTo: 'fields'
    },
    {
      name: 'standfirst',
      value: video.description ? '<p>' + video.description + '</p>' : null,
      belongsTo: 'fields'
    },
    {
      name: 'trailText',
      value: video.trailText ? '<p>' + video.trailText + '</p>' : null,
      belongsTo: 'fields'
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
