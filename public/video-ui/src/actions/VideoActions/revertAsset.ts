import { Dispatch } from "redux";
import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { setActiveAsset } from "../../slices/video";

function requestRevertAsset(assetVersion: number) {
  return {
    type: 'ASSET_REVERT_REQUEST',
    assetVersion: assetVersion,
    receivedAt: Date.now()
  };
}

function receiveRevertAsset(video: Video) {
  browserHistory.push('/videos/' + video.id);
  return setActiveAsset(video);
}

function errorRevertAsset(error: Error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not revert the asset',
    error: error,
    receivedAt: Date.now()
  };
}

export function revertAsset(atomId: string, version: number) {
  return (dispatch: Dispatch) => {
    dispatch(requestRevertAsset(version));
    return VideosApi.revertAsset(atomId, version)
      .then(video => dispatch(receiveRevertAsset(video)))
      .catch(error => dispatch(errorRevertAsset(error)));
  };
}
