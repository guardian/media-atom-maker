import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import { getComposerData } from '../util/getComposerData';
import { cleanVideoData } from '../util/cleanVideoData';
import ContentApi, { CapiContent, CapiContentResponse, Stage } from './capi';
import { ContentAtom, getVideoBlock } from '../util/getVideoBlock';

export type ComposerStage = 'live' | 'preview'

export type AssetType = 'Audio' | 'Video'

export type Platform = 'Youtube' | 'Facebook' | 'Dailymotion' | 'Mainstream' | 'Url'

export type Asset = {
  assetType: AssetType;
  version: number;
  id: string;
  platform: Platform;
  mimeType?: string;
}

export type User = {
  email: string;
  firstName?: string;
  lastName?: string
}

export type ChangeRecord = {
  date: string, // UNIX timestamp, ms
  user?: User
}

export type ContentChangeDetails = {
  lastModified?: ChangeRecord,
  created?: ChangeRecord,
  published?: ChangeRecord,
  revision: number,
  scheduledLaunch?: ChangeRecord,
  embargo?: ChangeRecord,
  expiry?: ChangeRecord
}

export type Video = {
  id: string;
  labels: string[];
  contentChangeDetails: ContentChangeDetails;
  assets: Asset[];
  activeVersion?: number;
  title: string;
  category: unknown;
  plutoData?: unknown;
  duration?: number;
  source?: string;
  description?: string;
  trailText?: string;
  posterImage?: unknown;
  trailImage?: unknown;
  youtubeOverrideImage: unknown;
  tags: string[];
  byline: string[];
  commissioningDesks: string[];
  keywords: string[];
  youtubeCategoryId?: string;
  license?: string;
  channelId?: string;
  legallySensitive: Boolean;
  sensitive?: Boolean;
  privacyStatus?: unknown;
  expiryDate?: number;
  youtubeTitle: string;
  youtubeDescription?: string;
  blockAds: Boolean;
  composerCommentsEnabled?: Boolean;
  optimisedForWeb?: Boolean;
  suppressRelatedContent?: Boolean;
}

type MediaAtomSummary = Pick<Video, 'id' | 'title' | 'contentChangeDetails' | 'posterImage'>

export type VideoWithoutId = Omit<Video, 'id'>

function getComposerUrl() {
  return getStore().getState().config.composerUrl;
}
function getUsages({ id, stage }: { id: string, stage: Stage }): Promise<CapiContent[]> {
  return pandaReqwest<{ response: { results: string[] } }>({
    url: `${ContentApi.getUrl(stage)}/atom/media/${id}/usage?page-size=100`
  }).then(res => {
    const usagePaths = res.response.results;

    // the atom usage endpoint in capi only returns article paths,
    // lookup the articles in capi to get their fields
    return Promise.all(
      usagePaths.map((path) => ContentApi.getByPath(path))
    ).then(capiResponse => {
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
    });
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

export default {
  fetchVideos: (search: string, limit: number, shouldUseCreatedDateForSort: boolean) => {
    let url = `/api/atoms?limit=${limit}`;
    if (search) {
      url += `&search=${search}`;
    }
    if (shouldUseCreatedDateForSort) {
      url += '&shouldUseCreatedDateForSort=true';
    }

    return pandaReqwest<{ total: number, atoms: MediaAtomSummary[] }>({
      url: url
    });
  },

  fetchVideo: (videoId: string) => {
    return pandaReqwest<Video>({
      url: '/api/atoms/' + videoId
    });
  },

  fetchPublishedVideo: (videoId: string) => {
    return pandaReqwest<Video>({
      url: '/api/atoms/' + videoId + '/published'
    });
  },

  createVideo: (video: VideoWithoutId) => {
    return pandaReqwest<Video>({
      url: '/api/atoms',
      method: 'post',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: cleanVideoData(video)
    });
  },

  publishVideo: (videoId: string) => {
    return pandaReqwest<Video>({
      url: '/api/atom/' + videoId + '/publish',
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      }
    });
  },

  createAsset: (asset: { uri: string }, videoId: string) => {
    return pandaReqwest<Video, { uri: string }>({
      url: '/api/atoms/' + videoId + '/assets',
      method: 'post',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: asset
    });
  },

  revertAsset: (atomId: string, version: number) => {
    return pandaReqwest<Video, { atomId: string, version: number }>({
      url: '/api/atom/' + atomId + '/asset-active',
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: { atomId, version }
    });
  },

  saveVideo: (videoId: string, video: VideoWithoutId) => {
    return pandaReqwest<Video>({
      url: '/api/atoms/' + videoId,
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: cleanVideoData(video)
    });
  },

  resetDurationFromActive: (videoId: string) =>
    pandaReqwest<Video>({
      url: `/api/atom/${videoId}/reset-duration-from-active`,
      method: 'put',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      }
    }),

  getVideoUsages: (videoId: string) => {
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
        data: {
          [ContentApi.preview]: splitPreview,
          [ContentApi.published]: splitPublished
        },

        // a lot of components conditionally render based on the number of usages,
        // rather than constantly call a utility function, let's cheat and put in in the object
        totalUsages: previewUsages.length + publishedUsages.length,
        totalVideoPages: splitPreview.video.length + splitPublished.video.length
      };
    });
  },

  deleteVideo: (videoId: string) => {
    return pandaReqwest<string>({
      url: '/api/atom/' + videoId,
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      method: 'delete'
    });
  },

  deleteAsset(video: Video, asset: Asset) {
    return pandaReqwest<Video, Asset>({
      url: `/api/atoms/${video.id}/assets`,
      method: 'delete',
      headers: {
        'Csrf-Token': (window as any).guardian.csrf.token
      },
      data: asset
    });
  },

  updateCanonicalPages(video: Video, usages: { data: Record<string, { video: CapiContent[] }> }, updatesTo: Stage) {
    const composerData = getComposerData(video);
    const composerUrlBase = getComposerUrl();
    const videoBlock = getVideoBlock(video.id, video.title, video.source);

    return Promise.all(
      Object.keys(usages.data).map(state => {
        const videoPageUsages: CapiContent[] = usages.data[state].video;

        return videoPageUsages.map(usage => {
          const pageId = usage.fields.internalComposerCode;

          if (updatesTo === state) {
            return pandaReqwest({
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

    return pandaReqwest({
      url: composerUrl,
      method: 'post',
      crossOrigin: true,
      withCredentials: true,
      data: {
        videoFields: cleanVideoData(composerData)
      }
    });
  },

  addVideoToComposerPage({ composerId, video }: { composerId: string, video: any }) {
    const composerUrlBase = getComposerUrl();

    const previewData = getVideoBlock(video.id, video.title, video.source);

    function updateMainBlock(stage: ComposerStage, data: { elements: ContentAtom[] }) {
      return pandaReqwest({
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
    return pandaReqwest<{ response: CapiContentResponse }>({
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
      return pandaReqwest({
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
