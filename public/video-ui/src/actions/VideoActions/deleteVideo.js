import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function errorVideoDelete(error) {
  return {
    type:       'SHOW_ERROR',
    message:    `Could not delete atom. ${error}`,
    error:      error,
    receivedAt: Date.now()
  };
}

export function deleteVideo(video) {
  return dispatch => {
    return VideosApi.deleteVideo(video.id)
        .then(() =>  {
          browserHistory.push('/videos');
        }).catch((error) => {
          dispatch(errorVideoDelete(error.response));
        });
  };
}