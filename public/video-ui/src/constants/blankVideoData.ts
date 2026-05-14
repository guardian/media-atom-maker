import { Video } from '../services/VideosApi';

export const blankVideoData: Video = {
  id: '',
  title: '',
  description: '',
  category: 'News',
  duration: 0,
  channelId: '',
  youtubeTitle: '',
  youtubeCategoryId: '',
  privacyStatus: '',
  assets: [],
  trailText: '',
  tags: [],
  byline: [],
  keywords: [],
  labels: [],
  composerCommentsEnabled: false,
  commissioningDesks: [],
  atomTagIds: [],
  contentChangeDetails: {
    revision: 0
  },
  blockAds: false,
  posterImage: {
    mediaId: '',
    assets: []
  },
  trailImage: {
    assets: []
  },
  youtubeOverrideImage: {
    assets: []
  }
};
