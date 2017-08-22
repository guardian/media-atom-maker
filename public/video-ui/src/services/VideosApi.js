import { pandaReqwest } from './pandaReqwest';
import { getStore } from '../util/storeAccessor';
import { getComposerData, getRightsPayload } from '../util/getComposerData';
import { nullifyEmptyStrings } from '../util/nullifyEmptyStrings';
import ContentApi from './capi';

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
  return usages.reduce((all, usage) => {
    if (usage.type === 'video') {
      all.video = [...all.video, usage];
    } else {
      all.other = [...all.other, usage];
    }
    return all;
  }, {video: [], other: []});
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
      data: nullifyEmptyStrings(video)
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
      data: nullifyEmptyStrings(video)
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

      const splitPreview = splitUsages({usages: draft});
      const splitPublished = splitUsages({usages: publishedUsages});

      return {
        data: {
          [ContentApi.preview]: splitPreview,
          [ContentApi.published]: splitPublished,
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

  updateCanonicalPages(video, composerUrlBase, videoBlock, usages) {
    function getComposerUpdateRequests({ composerId, video, composerUrlBase }) {
      const rightsExpiryPayload = getRightsPayload(video);
      const rightsRequest = pandaReqwest({
        url: `${composerUrlBase}/api/content/${composerId}/atom-expiry/rights`,
        method: 'post',
        crossOrigin: true,
        withCredentials: true,
        data: rightsExpiryPayload
      });

      // When article is in preview, composer keeps track of both the live and preview versions of an article
      // For both published and unpublished articles, we need to update both live and preview versions.
      const composerData = getComposerData(video);
      const dataUpdatePromises = composerData.reduce((promises, data) => {
        promises.push(updateArticleField('preview', data, composerUrlBase, composerId));
        promises.push(updateArticleField('live', data, composerUrlBase, composerId));

        return promises;
      }, []);

      dataUpdatePromises.push(rightsRequest);
      return dataUpdatePromises;
    }

    function updateArticleField(stage, data, composerUrl, pageId) {
      if (data.belongsTo === 'thumbnail') {
        if (data.value) {
          return pandaReqwest({
            url: `${composerUrl}/api/content/${pageId}/${stage}/thumbnail`,
            method: 'put',
            crossOrigin: true,
            withCredentials: true,
            data: data.value
          });
        } else {
          return pandaReqwest({
            url: `${composerUrl}/api/content/${pageId}/${stage}/thumbnail`,
            method: 'delete',
            crossOrigin: true,
            withCredentials: true
          });
        }
      }

      if (data.belongsTo === 'toolSettings') {
        return pandaReqwest({
          url: `${composerUrl}/api/content/${pageId}/toolSettings/${data.name}`,
          method: 'put',
          crossOrigin: true,
          withCredentials: true,
          data: `"${data.value}"`
        });
      }

      if (data.value || data.belongsTo === 'settings') {
        const value = data.isFreeText
          ? data.value.split('"').join('\\"')
          : data.value;
        return pandaReqwest({
          url: `${composerUrl}/api/content/${pageId}/${stage}/${data.belongsTo}/${data.name}`,
          method: 'put',
          crossOrigin: true,
          withCredentials: true,
          data: `"${value}"`
        });
      }

      return pandaReqwest({
        url: `${composerUrl}/api/content/${pageId}/${stage}/${data.belongsTo}/${data.name}`,
        method: 'delete',
        crossOrigin: true,
        withCredentials: true
      });
    }

    return Promise.all(Object.keys(usages.data).map(state => {
      const videoPageUsages = usages.data[state].video;

      return Promise.all(videoPageUsages.map(usage => {
        const composerId = usage.fields.internalComposerCode;

        const fieldPromises = getComposerUpdateRequests({
          composerId,
          video,
          composerUrlBase
        });

        const videoPagePromise = this.addVideoToComposerPage({
          composerId,
          previewData: videoBlock,
          composerUrlBase
        });

        return Promise.all([...fieldPromises, videoPagePromise]);
      }));
    })).then(() => {
      return Object.keys(usages.data).reduce((all, state) => {
        const updated = usages.data[state].video.map(usage => {
          return Object.assign({}, usage, {
            webTitle: video.title
          });
        });

        // TODO avoid mutation... but how on such a deep property?!
        all.data[state] = {
          video: updated,
          other: usages.data[state].other
        };

        return all;
      }, {
        data: {},
        totalUsages: usages.totalUsages,
        totalVideoPages: usages.totalVideoPages
      });
    });
  },

  createComposerPage(id, video, composerUrlBase) {
    const composerData = getComposerData(video);

    const composerUrl =
      composerUrlBase +
      '/api/content?atomPoweredVideo=true&originatingSystem=MediaAtomMaker&type=video';
    const videoFields = composerData.reduce((fields, data) => {
      fields[data.name] = data.value;
      return fields;
    }, {});

    return pandaReqwest({
      url: composerUrl,
      method: 'post',
      crossOrigin: true,
      withCredentials: true,
      data: {
        videoFields: nullifyEmptyStrings(videoFields)
      }
    });
  },

  addVideoToComposerPage({composerId, previewData, composerUrlBase}) {
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
