import VideosApi from '../../services/VideosApi';

function requestVideoPageUpdate() {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageUpdate(newTitle) {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    newTitle: newTitle,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoPageUpdate({error, message}) {
  return {
    type: 'SHOW_ERROR',
    message: message,
    error: error,
    receivedAt: Date.now()
  };
}

export function updateVideoPage(video, composerUrl, videoBlock, usages, updatesTo) {
  return dispatch => {
    dispatch(requestVideoPageUpdate());

    return VideosApi.updateCanonicalPages(
      video,
      composerUrl,
      videoBlock,
      usages,
      updatesTo
    )
      .then(() => dispatch(receiveVideoPageUpdate(video.title)))
      .catch(error => {
        const unknownError = 'An unknown error occurred. Please contact the Developers';

        try {
          const errorJson = JSON.parse(error.response);
          const errorKey = errorJson && errorJson.errorKey;

          const message = errorKey === 'insufficient-permission'
            ? `Could not update a Composer video page. You do not have sufficient Composer permissions (most likely <code>sensitivity_controls</code>). Please contact Central Production`
            : unknownError;

          dispatch(errorReceivingVideoPageUpdate({error, message}));
        } catch (e) {
          dispatch(errorReceivingVideoPageUpdate({error, message}));
        }
      });
  };
}
