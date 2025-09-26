import VideosApi from '../../services/VideosApi';
import { setPublishing } from '../../slices/saveState';
import { showError } from '../../slices/error';
import { AppDispatch } from '../../util/setupStore';
import { setVideoAndPublishedVideo } from '../../slices/video';

export function publishVideo(videoId: string) {
  return (dispatch: AppDispatch) => {
    dispatch(setPublishing(true));
    return VideosApi.publishVideo(videoId)
      .then(res => {
        dispatch(setPublishing(false));
        dispatch(setVideoAndPublishedVideo(res));
      })
      .catch(error => dispatch(showError(error.responseText, error)));
  };
}
