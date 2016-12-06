import VideosApi from '../../services/VideosApi';

function requestVideoPageCreate(id) {
  return {
    type:       'VIDEO_PAGE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageCreate(videoPage) {
  return {
    type:           'VIDEO_PAGE_POST_RECEIVE',
    videoPage:      videoPage,
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

export function createPage(id, title, data) {
  return dispatch => {
    dispatch(requestVideoPageCreate());
    return VideosApi.createVideoPage(id, title)
    .then(res => {
      return VideosApi.addVideoToPage(res.data.id, data)
    })
    .catch(error => {
      dispatch(errorReceivingVideoPageCreate(error))
    })
    .then(res => {
      dispatch(receiveVideoPageCreate(res))
    });
  };
}
