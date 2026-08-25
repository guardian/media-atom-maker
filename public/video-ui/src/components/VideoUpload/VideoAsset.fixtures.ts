import { blankVideoData } from '../../constants/blankVideoData';
import { Asset as VideoAsset, Video } from '../../services/VideosApi';
import { ClientAsset } from '../../slices/s3Upload';

export const defaultProps = {
  videoId: 'test-video-id',
  isActive: false,
  selectAsset: jest.fn(),
  deleteAsset: jest.fn(),
  startSubtitleFileUpload: jest.fn(),
  deleteSubtitle: jest.fn(),
  activatingAssetNumber: undefined
};

export const defaultStoreConfig = {
  permissions: {
    deleteAtom: true,
    setVideosOnAllChannelsPublic: true,
    pinboard: true,
    addSelfHostedAsset: true
  }
};

export const defaultVideoAsset: VideoAsset = {
  version: 1,
  id: 'AAAAAAAAAAA',
  assetType: 'Video',
  mimeType: 'video/youtube',
  platform: 'Youtube'
};

export const unpublishedVideo: Video = {
  ...blankVideoData,
  id: 'test-video-id',
  assets: [
    {
      ...defaultVideoAsset
    },
    {
      ...defaultVideoAsset,
      version: 2,
      id: 'BBBBBBBBBBB'
    }
  ]
};

export const publishedVideo: Video = {
  ...unpublishedVideo,
  activeVersion: 2,
  contentChangeDetails: {
    revision: 1,
    published: {
      date: 1787309822
    }
  }
};

export const completedUpload: ClientAsset = {
  id: '1',
  asset: {
    id: 'AAAAAAAAAAA'
  },
  metadata: {
    originalFilename: 'test.mov',
    startTimestamp: 1758557285850,
    user: 'a.person@example.co.uk'
  }
};

export const processingUpload: ClientAsset = {
  id: '2',
  processing: {
    status: 'Uploading to YouTube',
    failed: false,
    current: 0,
    total: 1
  },
  metadata: {
    originalFilename: 'test.mov',
    startTimestamp: 1758612923498,
    user: 'a.person@example.co.uk'
  }
};

export const failedUpload: ClientAsset = {
  ...processingUpload,
  processing: {
    status: 'Upload failed',
    failed: true
  }
};

export const unknownProgressUpload: ClientAsset = {
  ...processingUpload,
  processing: {
    status: 'Processing...',
    failed: false
  }
};

export const reprocessingUpload: ClientAsset = {
  id: '2',
  asset: {
    sources: [
      {
        src: 'https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.0.mp4',
        mimeType: 'video/mp4'
      },
      {
        src: 'https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.1.m3u8',
        mimeType: 'application/vnd.apple.mpegurl'
      }
    ]
  },
  processing: {
    status: 'SendToTranscoderV2',
    failed: false
  },
  metadata: {
    originalFilename: 'Video.mp4',
    startTimestamp: 1759499181730,
    subtitleFilename: 'subtitle.srt',
    user: 'a.person@example.co.uk'
  }
};

export const emptyUpload: ClientAsset = {
  id: '3',
  metadata: {
    user: 'a.person@example.co.uk'
  }
};
