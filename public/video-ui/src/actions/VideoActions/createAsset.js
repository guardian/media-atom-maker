import VideosApi from '../../services/VideosApi';
import { getVideo } from './getVideo';
import { getUploads } from '../UploadActions/getUploads';
import { setAssets } from "../../slices/video";
import { showError } from "../../slices/error";

const BLANK_ASSET = {
  uri: ''
};

function requestAssetCreate(video) {
  return {
    type: 'ASSET_CREATE_REQUEST',
    video: video,
    receivedAt: Date.now()
  };
}

export function createAsset(asset, video) {
  return dispatch => {
    dispatch(requestAssetCreate(video));
    return VideosApi.createAsset(asset, video.id)
      .then(res => {
        dispatch(setAssets(res));

        // Pull down the latest changes from the server
        dispatch(getUploads(video.id));
        dispatch(getVideo(video.id));
      })
      .catch(error => dispatch(showError(`Could not create asset. ${error.response}`, error)));
  };
}

export function populateEmptyAsset() {
  return {
    type: 'ASSET_POPULATE_BLANK',
    asset: Object.assign({}, BLANK_ASSET),
    receivedAt: Date.now()
  };
}
