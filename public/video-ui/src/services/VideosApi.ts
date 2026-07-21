import type { VideoPlayerFormat } from '../constants/videoCreateOptions';
import type { UsageData } from '../slices/usage';
import { cleanVideoData } from '../util/cleanVideoData';
import { getComposerData } from '../util/getComposerData';
import { ContentAtom, getVideoBlock } from '../util/getVideoBlock';
import { getStore } from '../util/storeAccessor';
import { apiRequest } from './apiRequest';
import ContentApi, { CapiContent, CapiContentResponse, Stage } from './capi';
import moment from 'moment';

export type ComposerStage = 'live' | 'preview';

export type AssetType = 'Audio' | 'Video' | 'Subtitles';

export type Platform =
  | 'Youtube'
  | 'Facebook'
  | 'Dailymotion'
  | 'Mainstream'
  | 'Url';

export type Asset = {
  assetType: AssetType;
  version: number;
  id: string;
  platform: Platform;
  mimeType?: string;
  aspectRatio?: string;
  dimensions?: {
    width: number;
    height: number;
  };
  duration?: number;
};

export type User = {
  email: string;
  firstName?: string;
  lastName?: string;
};

type ChangeRecord<T> = {
  date: T; // UNIX timestamp, ms or ISO 8601 string
  user?: User;
};

type ContentChangeDetailsBuilder<T> = {
  lastModified?: ChangeRecord<string>;
  created?: ChangeRecord<string>;
  published?: ChangeRecord<string>;
  revision: number;
  scheduledLaunch?: ChangeRecord<T>;
  embargo?: ChangeRecord<T>;
  expiry?: ChangeRecord<T>;
};

export type ContentChangeDetails = ContentChangeDetailsBuilder<number>;

export type PlutoData = {
  commissionId?: string;
  projectId?: string;
};

export type IconikData = {
  workingGroupId?: string;
  commissionId?: string;
  projectId?: string;
  masterPlaceholderId?: string;
};

export type ImageAsset = {
  mimeType?: string;
  file: string;
  dimensions?: {
    width: number;
    height: number;
  };
  size?: number;
  aspectRatio?: string;
};

export type Image = {
  mediaId: string;
  assets: ImageAsset[];
  master?: ImageAsset;
  source?: string;
};

export type Video = {
  id: string;
  type?: string;
  labels: string[];
  contentChangeDetails: ContentChangeDetails;
  assets: Asset[];
  activeVersion?: number;
  title: string;
  category: unknown;
  plutoData?: PlutoData;
  iconikData?: IconikData;
  duration?: number;
  source?: string;
  description?: string;
  trailText?: string;
  posterImage?: Image;
  trailImage?: unknown;
  youtubeOverrideImage: unknown;
  tags: string[];
  byline: string[];
  commissioningDesks: string[];
  atomTagIds: string[];
  keywords: string[];
  youtubeCategoryId?: string;
  license?: string;
  channelId?: string;
  legallySensitive?: boolean;
  sensitive?: boolean;
  privacyStatus?: unknown;
  expiryDate?: number;
  youtubeTitle: string;
  youtubeDescription?: string;
  blockAds: boolean;
  composerCommentsEnabled?: boolean;
  optimisedForWeb?: boolean;
  suppressRelatedContent?: boolean;
  videoPlayerFormat?: VideoPlayerFormat;
  platform?: Platform;
};

type VideoServer = Omit<Video, 'contentChangeDetails'> & {
  contentChangeDetails: ContentChangeDetailsBuilder<string>;
};

export type MediaAtomSummary = Pick<
  Video,
  'id' | 'title' | 'contentChangeDetails' | 'posterImage'
>;

type MediaAtomSummaryServer = Omit<MediaAtomSummary, 'contentChangeDetails'> & {
  contentChangeDetails: ContentChangeDetailsBuilder<string>;
};

export type VideoWithoutId = Omit<Video, 'id'>;

function getComposerUrl() {
  return getStore().getState().config.composerUrl;
}
function getUsages({
  id,
  stage
}: {
  id: string;
  stage: Stage;
}): Promise<CapiContent[]> {
  return apiRequest<{ response: { results: string[] } }>({
    url: `${ContentApi.getUrl(stage)}/atom/media/${id}/usage?page-size=100`
  }).then(res => {
    const usagePaths = res.response.results;

    // the atom usage endpoint in capi only returns article paths,
    // lookup the articles in capi to get their fields
    return Promise.all(usagePaths.map(path => ContentApi.getByPath(path))).then(
      capiResponse => {
        const usages = capiResponse.reduce((all: CapiContent[], item) => {
          return [...all, item.response.content];
        }, []);

        // sort by article creation date DESC
        usages.sort(
          (first, second) =>
            new Date(second.fields.creationDate).getDate() -
            new Date(first.fields.creationDate).getDate()
        );

        return usages;
      }
    );
  });
}

function splitUsages({ usages }: { usages: CapiContent[] }) {
  return usages.reduce(
    (all, usage) => {
      if (usage.type === 'video') {
        all.video = [...all.video, usage];
      } else {
        all.other = [...all.other, usage];
      }
      return all;
    },
    { video: [], other: [] }
  );
}

function dateAsMillis(d: string | undefined): number | undefined {
  if (d) {
    const result = moment(d).valueOf();
    return Number.isNaN(result) ? undefined : result;
  } else {
    return undefined;
  }
}

function clientVideoFromServerVideo<
  T extends { contentChangeDetails: ContentChangeDetailsBuilder<string> }
>(
  serverVideo: T
): Omit<T, 'contentChangeDetails'> & {
  contentChangeDetails: ContentChangeDetails;
} {
  // We and downstream consumers expect the scheduled launch to be an integer, but our API provides a string representation
  const { contentChangeDetails } = serverVideo;
  const scheduledLaunch = dateAsMillis(
    contentChangeDetails.scheduledLaunch?.date
  );
  const embargo = dateAsMillis(contentChangeDetails.embargo?.date);
  const expiry = dateAsMillis(contentChangeDetails.expiry?.date);
  return {
    ...serverVideo,
    contentChangeDetails: {
      ...contentChangeDetails,
      scheduledLaunch:
        scheduledLaunch !== undefined
          ? { ...contentChangeDetails.scheduledLaunch, date: scheduledLaunch }
          : undefined,
      embargo:
        embargo !== undefined
          ? { ...contentChangeDetails.embargo, date: embargo }
          : undefined,
      expiry:
        expiry !== undefined
          ? { ...contentChangeDetails.expiry, date: expiry }
          : undefined
    }
  };
}

export default {
  fetchVideos: (
    search: string,
    limit: number,
    shouldUseCreatedDateForSort: boolean,
    videoPlayerFormatFilter: string
  ): Promise<{ total: number; atoms: MediaAtomSummary[] }> => {
    let url = `/api/atoms?limit=${limit}`;
    if (search) {
      url += `&search=${search}`;
    }
    if (shouldUseCreatedDateForSort) {
      url += '&shouldUseCreatedDateForSort=true';
    }
    if (videoPlayerFormatFilter) {
      url += `&videoPlayerFormat=${videoPlayerFormatFilter}`;
    }

    return apiRequest<{ total: number; atoms: MediaAtomSummaryServer[] }>({
      url: url
    }).then(response => ({
      total: response.total,
      atoms: response.atoms.map(clientVideoFromServerVideo)
    }));
  },

  fetchVideo: (videoId: string) => {
    return apiRequest<VideoServer>({
      url: '/api/atoms/' + videoId
    }).then(clientVideoFromServerVideo);
  },

  fetchPublishedVideo: (videoId: string) => {
    return apiRequest<VideoServer>({
      url: '/api/atoms/' + videoId + '/published'
    }).then(clientVideoFromServerVideo);
  },

  createVideo: (video: VideoWithoutId) => {
    return apiRequest<VideoServer>({
      url: '/api/atoms',
      method: 'post',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: cleanVideoData(video)
    }).then(clientVideoFromServerVideo);
  },

  publishVideo: (videoId: string) => {
    return apiRequest<VideoServer>({
      url: '/api/atom/' + videoId + '/publish',
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      }
    }).then(clientVideoFromServerVideo);
  },

  createAsset: (asset: { uri: string }, videoId: string) => {
    return apiRequest<VideoServer, { uri: string }>({
      url: '/api/atoms/' + videoId + '/assets',
      method: 'post',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: asset
    }).then(clientVideoFromServerVideo);
  },

  revertAsset: (atomId: string, version: number) => {
    return apiRequest<VideoServer, { atomId: string; version: number }>({
      url: '/api/atom/' + atomId + '/asset-active',
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: { atomId, version }
    }).then(clientVideoFromServerVideo);
  },

  saveVideo: (videoId: string, video: VideoWithoutId) => {
    return apiRequest<VideoServer>({
      url: '/api/atoms/' + videoId,
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: cleanVideoData(video)
    }).then(clientVideoFromServerVideo);
  },

  resetDurationFromActive: (videoId: string) =>
    apiRequest<VideoServer>({
      url: `/api/atom/${videoId}/reset-duration-from-active`,
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      }
    }).then(clientVideoFromServerVideo),

  getVideoUsages: (videoId: string): Promise<UsageData> => {
    return Promise.all([
      getUsages({ id: videoId, stage: ContentApi.preview }),
      getUsages({ id: videoId, stage: ContentApi.published })
    ]).then(data => {
      const [previewUsages, publishedUsages] = data;

      // remove Published usages from Preview response
      const draft = [...previewUsages].filter(previewUsage => {
        return !publishedUsages.find(publishedUsage => {
          return publishedUsage.id === previewUsage.id;
        });
      });

      const splitPreview = splitUsages({ usages: draft });
      const splitPublished = splitUsages({ usages: publishedUsages });

      return {
        preview: splitPreview,
        published: splitPublished
      };
    });
  },

  deleteVideo: (videoId: string) => {
    return apiRequest<string>({
      url: '/api/atom/' + videoId,
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      method: 'delete'
    });
  },

  deleteAsset(video: Video, asset: Asset) {
    return apiRequest<VideoServer, Asset>({
      url: `/api/atoms/${video.id}/assets`,
      method: 'delete',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: asset
    }).then(clientVideoFromServerVideo);
  },

  deleteAssetList(video: Video, assets: Asset[]) {
    return apiRequest<VideoServer, Asset[]>({
      url: `/api/atoms/${video.id}/asset-list`,
      method: 'delete',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: assets
    }).then(clientVideoFromServerVideo);
  },

  updateCanonicalPages(video: Video, usageData: UsageData, updatesTo: Stage) {
    const composerData = getComposerData(video);
    const composerUrlBase = getComposerUrl();
    const videoBlock = getVideoBlock(video.id, video.title, video.source);

    return Promise.all(
      (Object.keys(usageData) as Stage[]).map(stage => {
        const videoPageUsages: CapiContent[] = usageData[stage].video;

        return videoPageUsages.map(usage => {
          const pageId = usage.fields.internalComposerCode;

          if (updatesTo === stage) {
            return apiRequest({
              url: `${composerUrlBase}/api/content/${pageId}/videopage`,
              method: 'put',
              crossOrigin: true,
              withCredentials: true,
              data: {
                videoFields: cleanVideoData(composerData),
                videoBlock: videoBlock
              }
            });
          }

          return Promise.resolve();
        });
      })
    );
  },

  createComposerPage(id: string, video: VideoWithoutId) {
    const composerData = getComposerData(video);
    const composerUrlBase = getComposerUrl();

    const composerUrl =
      composerUrlBase +
      '/api/content?atomPoweredVideo=true&originatingSystem=MediaAtomMaker&type=video';

    return apiRequest({
      url: composerUrl,
      method: 'post',
      crossOrigin: true,
      withCredentials: true,
      data: {
        videoFields: cleanVideoData(composerData)
      }
    });
  },

  addVideoToComposerPage({
    composerId,
    video
  }: {
    composerId: string;
    video: any;
  }) {
    const composerUrlBase = getComposerUrl();

    const previewData = getVideoBlock(video.id, video.title, video.source);

    function updateMainBlock(
      stage: ComposerStage,
      data: { elements: ContentAtom[] }
    ) {
      return apiRequest<{ data: { block: any } }>({
        url: `${composerUrlBase}/api/content/${composerId}/${stage}/mainblock`,
        method: 'post',
        crossOrigin: true,
        withCredentials: true,
        data: data
      });
    }

    // The composer client (whilst in draft) keeps both the preview and live data in sync so we must do the same

    return updateMainBlock('preview', previewData).then(preview => {
      const liveData = preview.data.block;

      return updateMainBlock('live', liveData);
    });
  },

  fetchComposerId(capiId: string): Promise<string> {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return apiRequest<{ response: CapiContentResponse }>({
      url: capiProxyUrl + '/' + capiId + '?show-fields=all'
    }).then(resp => {
      if (
        resp.response.content &&
        resp.response.content.fields &&
        resp.response.content.fields.internalComposerCode
      ) {
        return resp.response.content.fields.internalComposerCode;
      }
      return '';
    });
  },

  preventPublication(composerId: string) {
    const composerUrl = getComposerUrl();

    function doEmbargoIndefinitely(stage: ComposerStage) {
      return apiRequest({
        url: `${composerUrl}/api/content/${composerId}/${stage}/settings/embargoedIndefinitely`,
        method: 'put',
        crossOrigin: true,
        withCredentials: true,
        data: `"true"`
      });
    }

    return Promise.all([
      doEmbargoIndefinitely('preview'),
      doEmbargoIndefinitely('live')
    ]);
  }
};
