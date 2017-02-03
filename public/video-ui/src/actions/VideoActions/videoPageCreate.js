import VideosApi from '../../services/VideosApi';

function requestVideoPageCreate() {
  return {
    type:       'VIDEO_PAGE_CREATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageCreate(pageId, pagePath, videoId) {
  return {
    type:           'VIDEO_PAGE_CREATE_POST_RECEIVE',
    newPage:        { composerId: pageId, usage: pagePath, videoId: videoId },
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoPageCreate(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create a video page',
    error:      error,
    receivedAt: Date.now()
  };
}

export function createVideoPage(id, metadata, composerUrl, videoBlock) {
  return dispatch => {

    dispatch(requestVideoPageCreate());

    return VideosApi.createComposerPage(id, metadata, composerUrl)
    .then(res => {

      const pageId = res.data.id;
      const pagePath = res.data.identifiers.path.data;

      return VideosApi.addVideoToComposerPage(pageId, videoBlock, composerUrl)
      .then(() => {
        dispatch(receiveVideoPageCreate(pageId, pagePath, id));
      });
    })
    .catch(error => {
      dispatch(errorReceivingVideoPageCreate(error));
    });
  };
}
