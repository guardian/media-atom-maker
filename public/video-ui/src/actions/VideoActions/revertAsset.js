import { browserHistory } from 'react-router';
import VideosApi from '../../services/VideosApi';

function requestRevertAsset(assetVersion) {
  return {
    type: 'ASSET_REVERT_REQUEST',
    assetVersion: assetVersion,
    receivedAt: Date.now()
  };
}

function receiveRevertAsset(video) {
  browserHistory.push('/videos/' + video.id);
  return {
    type: 'ASSET_REVERT_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorRevertAsset(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not revert the asset',
    error: error,
    receivedAt: Date.now()
  };
}

export function revertAsset(atomId, videoId, version) {
  return dispatch => {
    dispatch(requestRevertAsset(version));
    return VideosApi.revertAsset(atomId, videoId)
      .then(res => dispatch(receiveRevertAsset(res)))
      .catch(error => dispatch(errorRevertAsset(error)));
  };
}
