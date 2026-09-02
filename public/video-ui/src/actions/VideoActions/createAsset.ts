import VideosApi from '../../services/VideosApi';
import { getVideo } from './getVideo';
import { setAddingAsset, setAssets } from '../../slices/video';
import { showError } from '../../slices/error';
import { getUploads } from '../../slices/uploads';
import { AppDispatch } from '../../util/setupStore';

export function createAsset(asset: { uri: string }, video: { id: string }) {
  return (dispatch: AppDispatch) => {
    dispatch(setAddingAsset(true));
    return VideosApi.createAsset(asset, video.id)
      .then(res => {
        dispatch(setAssets(res));
        // Pull down the latest changes from the server
        dispatch(getUploads(video.id));
        dispatch(getVideo(video.id));
      })
      .catch(error =>
        dispatch(showError(`Could not create asset. ${error.response}`, error))
      );
  };
}
