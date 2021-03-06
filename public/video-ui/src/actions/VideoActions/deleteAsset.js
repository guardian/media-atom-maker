import VideosApi from '../../services/VideosApi';
import {getUploads} from "../UploadActions/getUploads";

function requestAssetDelete(assetId) {
  return {
    type: 'ASSET_DELETE_REQUEST',
    assetId,
    receivedAt: Date.now()
  };
}

function receiveAssetDelete(video) {
  return {
    type: 'ASSET_DELETE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorAssetDelete(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not delete asset',
    error: error,
    receivedAt: Date.now()
  };
}

export function deleteAsset(video, assetId) {
  return dispatch => {
    dispatch(requestAssetDelete(assetId));

    const asset = video.assets.find(_ => _.id === assetId);

    return VideosApi.deleteAsset(video, asset)
      .then(res => {
        dispatch(receiveAssetDelete(res));

        // Pull down the latest changes from the server because uploads and assets are managed differently
        dispatch(getUploads(video.id));
      })
      .catch(err => dispatch(errorAssetDelete(err)));
  };
}
