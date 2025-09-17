import { Dispatch } from 'redux';
import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { setActiveAsset } from '../../slices/video';
import {
  setActivatingAssetNumber,
  setPublishing
} from '../../slices/saveState';
import { showError } from '../../slices/error';

function receiveRevertAsset(video: Video) {
  browserHistory.push('/videos/' + video.id);
  return setActiveAsset(video);
}

export function revertAsset(atomId: string, version: number) {
  return (dispatch: Dispatch) => {
    dispatch(setActivatingAssetNumber(version));
    return VideosApi.revertAsset(atomId, version)
      .then(video => {
        dispatch(setActivatingAssetNumber());
        dispatch(receiveRevertAsset(video));
      })
      .catch(error => dispatch(showError('Failed to activate asset', error)));
  };
}
