import VideosApi from '../../services/VideosApi';

function requestVideos(search, limit) {
  return {
    type: 'VIDEOS_GET_REQUEST',
    search: search,
    limit: limit,
    receivedAt: Date.now()
  };
}

function receiveVideos(total, videos) {
  return {
    type: 'VIDEOS_GET_RECEIVE',
    total: total,
    videos: videos,
    receivedAt: Date.now()
  };
}

function errorReceivingVideos(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get videos',
    error: error,
    receivedAt: Date.now()
  };
}

export function getVideos(search, limit) {
  return dispatch => {
    dispatch(requestVideos(search, limit));
    return VideosApi.fetchVideos(search, limit)
      .then(res => {
        dispatch(receiveVideos(res.total, res.atoms));
      })
      .catch(error => dispatch(errorReceivingVideos(error)));
  };
}
