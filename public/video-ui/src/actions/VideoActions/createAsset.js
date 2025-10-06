import VideosApi from '../../services/VideosApi';
import { getVideo } from './getVideo';
import {setAddingAssetState, setAssets} from "../../slices/video";
import { showError } from "../../slices/error";
import { getUploads } from "../../slices/uploads";

export function createAsset(asset, video) {
  return dispatch => {
    dispatch(setAddingAssetState(true));
    return VideosApi.createAsset(asset, video.id)
      .then(res => {
        dispatch(setAddingAssetState(false));
        dispatch(setAssets(res));
        // Pull down the latest changes from the server
        dispatch(getUploads(video.id));
        dispatch(getVideo(video.id));
      })
      .catch(error => dispatch(showError(`Could not create asset. ${error.response}`, error)));
  };
}
