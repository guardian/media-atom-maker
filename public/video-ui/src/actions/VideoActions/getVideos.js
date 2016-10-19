import VideosApi from '../../services/VideosApi';

function requestVideos() {
  return {
    type:       'VIDEOS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function recieveVideos(videos) {
  return {
    type:       'VIDEOS_GET_RECIEVE',
    videos:     videos,
    receivedAt: Date.now()
  };
}

function errorRecievingVideos(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get videos',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getVideos() {
  return dispatch => {
    dispatch(requestVideos());
    return VideosApi.fetchVideos()
        .catch(error => dispatch(errorRecievingVideos(error)))
        .then(res => {
          dispatch(recieveVideos(res));
        });
  };
}
