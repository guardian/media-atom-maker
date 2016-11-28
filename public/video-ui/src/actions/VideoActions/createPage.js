import VideosApi from '../../services/VideosApi';

function requestVideo(id) {
  return {
    type:       'VIDEO_PAGE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideo(video) {
  return {
    type:       'VIDEO_PAGE_POST_RECEIVE',
    video:      video,
    receivedAt: Date.now()
  };
}

function errorReceivingVideo(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create a video page',
    error:      error,
    receivedAt: Date.now()
  };
}

export function createPage(id, title) {
  return dispatch => {
    dispatch(requestVideo());
    return VideosApi.createVideoPage(id, title)
        .catch(error => dispatch(errorReceivingVideo(error)))
        .then(res => {
          dispatch(receiveVideo(res));
        });
  };
}
