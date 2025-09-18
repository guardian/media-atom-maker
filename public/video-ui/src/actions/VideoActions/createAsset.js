import VideosApi from '../../services/VideosApi';
import { getVideo } from './getVideo';
import { getUploads } from '../UploadActions/getUploads';
import { setAssets } from "../../slices/video";
import { showError } from "../../slices/error";
import { setAddingAsset } from "../../slices/saveState";

export function createAsset(asset, video) {
  return dispatch => {
    dispatch(setAddingAsset(true));
    return VideosApi.createAsset(asset, video.id)
      .then(res => {
        dispatch(setAddingAsset(false));
        dispatch(setAssets(res));
        // Pull down the latest changes from the server
        dispatch(getUploads(video.id));
        dispatch(getVideo(video.id));
      })
      .catch(error => dispatch(showError(`Could not create asset. ${error.response}`, error)));
  };
}
