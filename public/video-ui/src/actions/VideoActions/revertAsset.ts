import { Dispatch } from "redux";
import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { setActiveAsset } from "../../slices/video";
import { showError } from "../../slices/error";

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

export function revertAsset(atomId: string, version: number) {
  return (dispatch: Dispatch) => {
    dispatch(requestRevertAsset(version));
    return VideosApi.revertAsset(atomId, version)
      .then(video => dispatch(receiveRevertAsset(video)))
      .catch(error => {
        dispatch(showError('Failed to activate asset', error))});
  };
}
