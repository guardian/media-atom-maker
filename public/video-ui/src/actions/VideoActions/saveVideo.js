import VideosApi from '../../services/VideosApi';

function requestVideoSave(video) {
  return {
    type: 'VIDEO_SAVE_REQUEST',
    video: video,
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

function receiveVideoSave(video) {
  return {
    type: 'VIDEO_SAVE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages) {
  return {
    type: 'VIDEO_USAGE_GET_RECEIVE',
    usages: usages,
    receivedAt: Date.now()
  };
}

function errorVideoSave(error, message) {
  return {
    type: 'SHOW_ERROR',
    message,
    error: error,
    receivedAt: Date.now()
  };
}

export function saveVideo(video) {
  return dispatch => {
    dispatch(requestVideoSave(video));
    return VideosApi.saveVideo(video.id, video)
    .then(res => {
      dispatch(receiveVideoSave(res))
      return VideosApi.getVideoUsages(video.id)
    })
    .then(usages => {
      dispatch(receiveVideoUsages(usages));
      return VideosApi.updateCanonicalPages(
        video,
        usages,
        'preview'
      )
      .then(() => dispatch(receiveVideoPageUpdate(video.title)))
    })
    .catch(error => {
      const defaultError = 'Could not save video';
      const conflictError = 'Could not save video as another user (or tab) is currently editing this video';
      const message = error.status === 409 ? conflictError : defaultError;
      dispatch(errorVideoSave(error, message));
    });
  };
}
