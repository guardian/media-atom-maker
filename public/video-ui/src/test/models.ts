import { CropOption } from '../services/GridMetadata';
import { Asset, Image, Video } from '../services/VideosApi';
import { DEFAULT_VIDEO_RATIO } from '../util/getAspectRatio';

export const emptyVideo: Video = {
  id: '',
  labels: [],
  contentChangeDetails: {
    lastModified: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    },
    created: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    },
    published: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    },
    revision: 0,
    scheduledLaunch: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    },
    embargo: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    },
    expiry: {
      date: 0,
      user: {
        email: '',
        firstName: '',
        lastName: ''
      }
    }
  },
  assets: [],
  title: '',
  category: undefined,
  youtubeOverrideImage: undefined,
  tags: [],
  byline: [],
  commissioningDesks: [],
  keywords: [],
  youtubeTitle: '',
  blockAds: undefined,
  atomTagIds: []
};
export const videoAsset1: Asset = {
  assetType: 'Video',
  version: 1,
  id: 'id',
  platform: 'Youtube'
};
export const videoAsset2: Asset = {
  assetType: 'Video',
  version: 2,
  id: 'id',
  platform: 'Youtube'
};
export const audioAsset1: Asset = {
  assetType: 'Audio',
  version: 1,
  id: 'id',
  platform: 'Youtube'
};
export const videoWithActiveAsset5by4 = {
  ...emptyVideo,
  activeVersion: 1,
  assets: [
    {
      ...videoAsset1,
      aspectRatio: '5:4'
    }
  ]
};

export const emptyImage: Image = {
  mediaId: 'mediaId',
  assets: []
};
export const imageWithAspectRatio5by3: Image = {
  mediaId: 'mediaId',
  assets: [],
  master: {
    file: 'file',
    aspectRatio: '5:3'
  }
};

export const imageWithAspectRatio5by4: Image = {
  mediaId: 'mediaId',
  assets: [],
  master: {
    file: 'file',
    aspectRatio: '5:4'
  }
};

export const imageWithDefaultVideoAspectRatio: Image = {
  mediaId: 'mediaId',
  assets: [],
  master: {
    file: 'file',
    aspectRatio: DEFAULT_VIDEO_RATIO
  }
};

export const cropOption5by4: CropOption = {
  key: 'landscape',
  ratio: '5 / 4',
  ratioString: '5:4'
};
