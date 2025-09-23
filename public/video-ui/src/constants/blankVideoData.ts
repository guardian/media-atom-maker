import { Video } from "../services/VideosApi";

export const blankVideoData: Video = {
  id: '',
  title: '',
  description: '',
  category: 'News',
  duration: 1,
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
  contentChangeDetails: {
    revision: 0
  },
  blockAds: false,
  posterImage: {
    assets: []
  },
  trailImage: {
    assets: []
  },
  youtubeOverrideImage: {
    assets: []
  }
};
