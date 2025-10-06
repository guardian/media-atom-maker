import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { AppDispatch } from '../../util/setupStore';
import {setVideoAndPublishedVideo, setVideoPublishingState} from '../../slices/video';

export function publishVideo(videoId: string) {
  return (dispatch: AppDispatch) => {
    dispatch(setVideoPublishingState(true));
    return VideosApi.publishVideo(videoId)
      .then(res => {
        dispatch(setVideoPublishingState(false));
        dispatch(setVideoAndPublishedVideo(res));
      })
      .catch(error => dispatch(showError(error.responseText, error)));
  };
}
