import PlutoVideosApi from '../../services/PlutoVideosApi';
import Logger from '../../logger';

function requestPlutoVideos() {
  return {
    type: 'PLUTO_VIDEOS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receivePlutoVideos(videos) {
  return {
    type: 'PLUTO_VIDEOS_GET_RECEIVE',
    videos: videos,
    receivedAt: Date.now()
  };
}

function errorReceivingPlutoVideos(error) {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get videos without pluto ids',
    error: error,
    receivedAt: Date.now()
  };
}

export function getPlutoVideos() {
  return dispatch => {
    dispatch(requestPlutoVideos());
    return PlutoVideosApi.fetchPlutoVideos()
      .then(res => {
        dispatch(receivePlutoVideos(res));
      })
      .catch(error => dispatch(errorReceivingPlutoVideos(error)));
  };
}
