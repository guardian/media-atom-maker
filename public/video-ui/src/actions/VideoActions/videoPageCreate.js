import VideosApi from '../../services/VideosApi';
import ContentApi from '../../services/capi';

function requestVideoPageCreate() {
  return {
    type: 'VIDEO_PAGE_CREATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageCreate(capiPage) {
  return {
    type: 'VIDEO_PAGE_CREATE_POST_RECEIVE',
    newPage: capiPage,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoPageCreate(error) {
  console.log('got error ', error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not create a video page',
    error: error,
    receivedAt: Date.now()
  };
}

export function createVideoPage(id, video, composerUrl, videoBlock) {
  return dispatch => {
    dispatch(requestVideoPageCreate());

    return VideosApi.createComposerPage(id, video, composerUrl)
      .then(res => {
        const pageId = res.data.id;
        const pagePath = res.data.identifiers.path.data;

        return VideosApi.addVideoToComposerPage(
          pageId,
          videoBlock,
          composerUrl,
          false
        ).then(() => {
          // it takes a little time for the new Composer page to get to CAPI,
          // so keep trying until success or timeout
          return ContentApi.getByPath(pagePath, true).then(capiResponse => {
            return dispatch(
              receiveVideoPageCreate(capiResponse.response.content)
            );
          });
        });
      })
      .catch(error => {
        dispatch(errorReceivingVideoPageCreate(error));
      });
  };
}
