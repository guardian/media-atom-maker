import VideosApi, { Video } from '../../services/VideosApi';
import { setVideo } from "../../slices/video";
import { showError } from "../../slices/error";
import { AppDispatch } from "../../util/setupStore";
import { getUploads } from "../../slices/uploads";

function requestAssetDelete(assetId: string) {
  return {
    type: 'ASSET_DELETE_REQUEST',
    assetId,
    receivedAt: Date.now()
  };
}

export function deleteAsset(video: Video, assetId: string) {
  return (dispatch: AppDispatch) => {
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

export function deleteAssets(video: Video, assetIds: string[]) {
  return (dispatch: AppDispatch) => {
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
