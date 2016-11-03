import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function requestRevertAsset() {
  return {
    type:       'ASSET_REVERT_REQUEST',
    receivedAt: Date.now()
  };
}

function recieveRevertAsset(video) {
  browserHistory.push('/video/videos/' + video.id);
  return {
    type: 'ASSET_REVERT_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };

}

function errorRevertAsset(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create video',
    error:      error,
    receivedAt: Date.now()
  };
}

export function revertAsset(version, videoId) {
  return dispatch => {
    dispatch(requestRevertAsset());
    return VideosApi.revertAsset(version, videoId)
        .then(res => dispatch(recieveRevertAsset(res)))
        .fail(error => dispatch(errorRevertAsset(error)));
  };
}
