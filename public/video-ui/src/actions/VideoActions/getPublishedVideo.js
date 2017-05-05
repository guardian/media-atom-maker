import VideosApi from '../../services/VideosApi';

function requestPublishedVideo(id) {
  return {
    type: 'PUBLISHED_VIDEO_GET_REQUEST',
    id: id,
    receivedAt: Date.now()
  };
}

function receivePublishedVideo(video) {
  return {
    type: 'PUBLISHED_VIDEO_GET_RECEIVE',
    publishedVideo: video,
    receivedAt: Date.now()
  };
}

function errorReceivingPublishedVideo(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get published video',
    error: error,
    receivedAt: Date.now()
  };
}

export function getPublishedVideo(id) {
  return dispatch => {
    dispatch(requestPublishedVideo(id));
    return VideosApi.fetchPublishedVideo(id)
      .then(video => {
        dispatch(receivePublishedVideo(video));
      })
      .catch(error => {
        dispatch(errorReceivingPublishedVideo(error));
      });
  };
}
