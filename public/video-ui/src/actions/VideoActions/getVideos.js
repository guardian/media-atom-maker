import VideosApi from '../../services/VideosApi';

function requestVideos(search, limit, shouldUseCreatedDateForSort) {
  return {
    type: 'VIDEOS_GET_REQUEST',
    search,
    limit,
    shouldUseCreatedDateForSort,
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

export function getVideos(search, limit, shouldUseCreatedDateForSort) {
  return dispatch => {
    dispatch(requestVideos(search, limit, shouldUseCreatedDateForSort));
    return VideosApi.fetchVideos(search, limit, shouldUseCreatedDateForSort)
      .then(res => {
        dispatch(receiveVideos(res.total, res.atoms));
      })
      .catch(error => dispatch(errorReceivingVideos(error)));
  };
}
