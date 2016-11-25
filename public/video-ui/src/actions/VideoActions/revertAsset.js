import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function requestRevertAsset(assetVersion) {
  return {
    type:       'ASSET_REVERT_REQUEST',
    assetVersion: assetVersion,
    receivedAt: Date.now()
  };
}

function receiveRevertAsset(video) {
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
    message:    'Could not revert the asset',
    error:      error,
    receivedAt: Date.now()
  };
}

export function revertAsset(version, videoId) {
  return dispatch => {
    dispatch(requestRevertAsset(version));
    return VideosApi.revertAsset(version, videoId)
        .then(res => dispatch(receiveRevertAsset(res)))
        .catch(error => dispatch(errorRevertAsset(error)));
  };
}
