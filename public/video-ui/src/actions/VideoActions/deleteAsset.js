import VideosApi from '../../services/VideosApi';
import { getUploads } from "../UploadActions/getUploads";
import { setVideo } from "../../slices/video";
import { showError } from "../../slices/error";

function requestAssetDelete(assetId) {
  return {
    type: 'ASSET_DELETE_REQUEST',
    assetId,
    receivedAt: Date.now()
  };
}

export function deleteAsset(video, assetId) {
  return dispatch => {
    dispatch(requestAssetDelete(assetId));

    const asset = video.assets.find(_ => _.id === assetId);

    return VideosApi.deleteAsset(video, asset)
      .then(res => {
        dispatch(setVideo(res));

        // Pull down the latest changes from the server because uploads and assets are managed differently
        dispatch(getUploads(video.id));
      })
      .catch(err => dispatch(showError('Could not delete asset', err)));
  };
}

export function deleteAssets(video, assetIds) {
  return dispatch => {
    assetIds.forEach((assetId) => {
      dispatch(requestAssetDelete(assetId));
    });

    const assets = video.assets.filter(_ => assetIds.includes(_.id));

    return VideosApi.deleteAssetList(video, assets)
      .then(res => {
        dispatch(setVideo(res));

        // Pull down the latest changes from the server because uploads and assets are managed differently
        dispatch(getUploads(video.id));
      })
      .catch(err => dispatch(showError('Could not delete asset', err)));
  };
}
