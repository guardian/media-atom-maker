import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import { getComposerData } from '../util/getComposerData';
import { cleanVideoData } from '../util/cleanVideoData';
import ContentApi from './capi';
import { getVideoBlock } from '../util/getVideoBlock';

function getUsages({ id, stage }) {
  return pandaReqwest({
    url: `${ContentApi.getUrl(stage)}/atom/media/${id}/usage`
  }).then(res => {
    const usagePaths = res.response.results;

    // the atom usage endpoint in capi only returns article paths,
    // lookup the articles in capi to get their fields
    return Promise.all(
      usagePaths.map(ContentApi.getByPath)
    ).then(capiResponse => {
      const usages = capiResponse.reduce((all, item) => {
        return [...all, item.response.content];
      }, []);

      // sort by article creation date DESC
      usages.sort(
        (first, second) =>
          new Date(second.fields.creationDate) -
          new Date(first.fields.creationDate)
      );

      return usages;
    });
  });
}

function splitUsages({ usages }) {
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
  fetchVideos: (search, limit) => {
    let url = `/api2/atoms?limit=${limit}`;
    if (search) {
      url += `&search=${search}`;
    }

    return pandaReqwest({
      url: url
    });
  },

  fetchVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId
    });
  },

  fetchPublishedVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/published'
    });
  },

  createVideo: video => {
    return pandaReqwest({
      url: '/api2/atoms',
      method: 'post',
      data: cleanVideoData(video)
    });
  },

  publishVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId + '/publish',
      method: 'put'
    });
  },

  createAsset: (asset, videoId) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId + '/assets',
      method: 'post',
      data: asset
    });
  },

  revertAsset: (atomId, version) => {
    return pandaReqwest({
      url: '/api2/atom/' + atomId + '/asset-active',
      method: 'put',
      data: { version }
    });
  },

  saveVideo: (videoId, video) => {
    return pandaReqwest({
      url: '/api2/atoms/' + videoId,
      method: 'put',
      data: cleanVideoData(video)
    });
  },

  fetchAudits: atomId => {
    return pandaReqwest({
      url: '/api2/audits/' + atomId
    });
  },

  getVideoUsages: videoId => {
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

  deleteVideo: videoId => {
    return pandaReqwest({
      url: '/api2/atom/' + videoId,
      method: 'delete'
    });
  },

  updateCanonicalPages(video, composerUrlBase, usages, updatesTo) {
    const composerData = getComposerData(video);
    const videoBlock = getVideoBlock(
      video.id,
      video.title,
      video.source
    );

    return Promise.all(
      Object.keys(usages.data).map(state => {
        const videoPageUsages = usages.data[state].video;

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

  createComposerPage(id, video, composerUrlBase) {
    const composerData = getComposerData(video);

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

  addVideoToComposerPage({ composerId, composerUrlBase, video}) {

    const previewData = getVideoBlock(
      video.id,
      video.title,
      video.source
    );

    function updateMainBlock(stage, data) {
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

  fetchComposerId(capiId) {
    const capiProxyUrl = getStore().getState().config.capiProxyUrl;
    return pandaReqwest({
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

  preventPublication(composerId, composerUrl) {
    function doEmbargoIndefinitely(stage) {
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
